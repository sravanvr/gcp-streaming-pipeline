package io.github.streamingpipeline.utils;

import java.io.IOException;

import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.BigtableDataClientFactory;
import com.google.cloud.bigtable.data.v2.BigtableDataSettings;
import com.google.cloud.bigtable.data.v2.models.Query;
import com.google.cloud.bigtable.data.v2.models.Row;
import com.google.cloud.bigtable.data.v2.models.RowMutation;
import com.google.protobuf.ByteString;
import com.google.inject.Inject;
import io.github.streamingpipeline.TxnstBigtableOptions;
import io.github.streamingpipeline.config.ApplicationProperties;
import org.apache.hadoop.hbase.shaded.org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.cloud.bigtable.data.v2.models.Filters.FILTERS;

public class BigtableClientUtil {

	BigtableDataClient dataClient = null;
	ApplicationProperties properties;
	private static final Logger logger = LoggerFactory.getLogger(BigtableClientUtil.class);

	@Inject
	public BigtableClientUtil(ApplicationProperties properties) throws IOException {
		String projectId = properties.getPipelineOptions().getProject();
		String instanceId = properties.getPipelineOptions().getBigtableInstanceId().get();
		this.properties = properties;

		BigtableDataSettings bigtableDataSettings = BigtableDataSettings.newBuilder()
				.setProjectId(projectId)
				.setInstanceId(instanceId)
				.build();

		BigtableDataClientFactory clientFactory = BigtableDataClientFactory.create(bigtableDataSettings);//NOSONAR This is closed in Transform Teardown call.
		dataClient = clientFactory.createForAppProfile(properties.getPipelineOptions().getAppProfileId().get());
	}

	public void writeLookupTableRow(String rowkey, String lookupRowValue) {
		long currentMicros = System.currentTimeMillis() * 1000;
		RowMutation rowMutation =
				RowMutation.create(properties.getPipelineOptions().getLookupTableId().get(), rowkey)
						.setCell(
								Constants.LOOKUP_COLUMN_FAMILY,
								ByteString.copyFrom(Constants.LOOKUP_COLUMN_QUALIFIER.getBytes()),
								currentMicros,
								ByteString.copyFrom(lookupRowValue.getBytes()));

		dataClient.mutateRow(rowMutation);
	}

	public void writeMainTableRow(String rowkey, String mainTableRowValue, String tags) {
		long currentMicros = System.currentTimeMillis() * 1000;
		RowMutation rowMutation =
				RowMutation.create(properties.getPipelineOptions().getAccountTableId().get(), rowkey)
						.setCell(
								Constants.COLUMN_FAMILY,
								ByteString.copyFrom(Constants.ACCOUNT_COLUMN_QUALIFIER.getBytes()),
								currentMicros,
								ByteString.copyFrom(mainTableRowValue.getBytes()))
						.setCell(
								Constants.COLUMN_FAMILY,
								ByteString.copyFrom(Constants.TAG_COLUMN_QUALIFIER.getBytes()),
								currentMicros,
								ByteString.copyFrom(tags.getBytes()));

		dataClient.mutateRow(rowMutation);
	}

	private String readResult(String rowkey,String tableId) {
		String output = "";
		try{
			Row row = dataClient.readRow(tableId, rowkey);
			if (row != null && CollectionUtils.isNotEmpty(row.getCells())) {
				output = row.getCells().get(0).getValue().toStringUtf8();
			}
		} catch (Exception e) {
			logger.error("Error while retrieving Bigtable Data. tableId = {}, rowKey = {}, Exception = {}", tableId, rowkey, e.getMessage());
		}
		return output;
	}

	public String getRowKey(String rowKey, String tableId){

		String result = getCache().getIfPresent(rowKey);
		if (result == null ){
			result = readResult(rowKey, tableId);
			updateCache(rowKey, result);
		}
		return result;
	}

	public synchronized void updateCache(String rowkey, String result) {
		if (result != null) {
			getCache().put(rowkey, result);
		}
	}

	private static Cache<String, String> localCache = null;

	private static Cache<String, String> getCache(){
		if (localCache == null) {
			localCache = CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.MINUTES).build();
		}
		return localCache;
	}

	public void closeConnection()  {
		synchronized(BigtableClientUtil.class) {
			if (dataClient != null) {
				dataClient.close();
				dataClient = null;
			}
		}
	}


	public long countRecordsForBatch(
			TxnstBigtableOptions options,
			BigtableDataClient dataClient,
			String importId,
			String institutionId
	) {

		var lookupFilter =
				FILTERS.key().regex(".+#iid#" + importId);
		var limitCellsFilter = FILTERS.limit().cellsPerRow(1);
		var stripValueFilter = FILTERS.value().strip();
		var filter = FILTERS.chain()
				.filter(lookupFilter)
				.filter(limitCellsFilter)
				.filter(stripValueFilter);

		var query = Query.create(options.getAccountTableId().get())
				.prefix(institutionId + "#ci#")
				.filter(filter);
		var rows = dataClient.readRows(query);
		return StreamSupport.stream(rows.spliterator(), true).count();
	}

	public Stream<Row> getRecordsForBatch(
			TxnstBigtableOptions options,
			BigtableDataClient dataClient,
			String importId,
			String institutionId
	) {
		var lookupFilter =
				FILTERS.key().regex(".+#iid#" + importId);

		var query = Query.create(options.getAccountTableId().get())
				.prefix(institutionId + "#ci#")
				.filter(lookupFilter);
		var rows = dataClient.readRows(query);
		return StreamSupport.stream(rows.spliterator(), true).parallel();
	}
}