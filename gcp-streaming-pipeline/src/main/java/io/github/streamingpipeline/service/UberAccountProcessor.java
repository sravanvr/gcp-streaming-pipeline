package io.github.streamingpipeline.service;

import com.google.bigtable.v2.Mutation;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.protobuf.ByteString;
import io.github.streamingpipeline.config.ApplicationProperties;
import io.github.streamingpipeline.exception.AccountsPipelineException;
import io.github.streamingpipeline.model.UberAccount;
import io.github.streamingpipeline.model.UberAccountDetail;
import io.github.streamingpipeline.model.ErrorLog;
import io.github.streamingpipeline.model.Institution;
import io.github.streamingpipeline.rowkey.UberAccountLookupRowKey;
import io.github.streamingpipeline.utils.BigtableClientUtil;
import org.apache.beam.repackaged.core.org.apache.commons.lang3.StringUtils;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.streamingpipeline.utils.Constants.FILE_NAME;

/***
 * 
 * @author Sravan Vedala
 *
 */
public class UberAccountProcessor {

	@Inject
	MetadataClient metadataClient;

	@Inject
	BigtableClientUtil bigtableClientUtil;

	@Inject
	ApplicationProperties applicationProperties;

	@Inject
	UberAccountLookupRowKey accountLookup;
	
	@Inject
	ImportManager importManager;

	@Inject
	@Named ("getGson")
	Gson gson;
	
	@Inject
	ErrorMessageBuilder logMessageBuilder;
	
	private static final Logger logger = LoggerFactory.getLogger(UberAccountProcessor.class);
		
	/**
	 * Main Process Function
	 * @param processContext
	 * @throws AccountsPipelineException
	 */
	public void process(DoFn<String, KV<ByteString, Iterable<Mutation>>>.ProcessContext processContext) throws AccountsPipelineException {	
		
		List<ErrorLog> errorLogList = new ArrayList<ErrorLog>();		
		String pubsubMessage = processContext.element();
		List<UberAccountDetail> accounts = null;
		try {
			/* Validate Accounts message pulled from pub-sub topic */
			JsonElement element = importManager.validatePubSubMessage(pubsubMessage);		

			/* Process Accounts block */		
			JsonArray array = element.getAsJsonArray();
			accounts = importManager.jsonArrayToAccount(array);

			Institution institution = importManager.getInstitutionConfig(accounts.get(0));

			accounts.stream().forEach(accountDetail -> {
				try {
					// process each account
					processAccount(processContext, accountDetail, institution);
					logger.info("ACCOUNT_BLOCK_IMPORT_SUCCESS - Accounts block for Institution {} has been successfully imported", institution.getInstitutionId());
				} catch (AccountsPipelineException ape) {
					importManager.handleException(ape, accountDetail, errorLogList);
				} catch (Exception ex) {
					importManager.handleException(ex, accountDetail, errorLogList);
				}
			});

		} catch (Exception e) {
			importManager.handleException(e, accounts, pubsubMessage, errorLogList);
		} finally {
			importManager.insertErrorLog(errorLogList);
		}
	}

	private void processAccount(DoFn<String, KV<ByteString, Iterable<Mutation>>>.ProcessContext processContext,
								UberAccountDetail accountDetail, Institution institution) {

		String messageSourceDetails = logMessageBuilder.getMessageSourceDetails(accountDetail);
		logger.debug("Input Accounts message : {}", accountDetail.getRecord());
		
		UberAccount account = accountDetail.getRecord();
		String tagsJsonString = importManager.getTags(accountDetail);

		String lookUpTableId = applicationProperties.getPipelineOptions().getLookupTableId().get();
		Map<String, TupleTag<KV<ByteString, Iterable<Mutation>>>> tupleTagsMap = applicationProperties.getTupleTagsMap();  

		String accountLookupRowKey = accountLookup.buildAccountLookupRowKey(account, institution);

		String accountGUID = bigtableClientUtil.getRowKey(accountLookupRowKey, lookUpTableId);

		// Lookup key found. Use accountGUID value to update existing Account record.
		if(StringUtils.isNotBlank(accountGUID)) {
			logger.debug("Account GUID already exist in Lookup table -  accountLookupRowKey: {} | accountGUID: {} | Message source: {}", accountLookupRowKey, accountGUID, messageSourceDetails);
			account.setId(accountGUID);
			
			// Insert Main Table Row
			String accountJsonString = gson.toJson(account, UberAccount.class);
			String accountRowKey = BigtableUtil.buildAccountRowKey(account);
			bigtableClientUtil.writeMainTableRow(accountRowKey, accountJsonString, tagsJsonString);
			logger.debug("Inserted account record with RowKey: {} | Message Source: {}", accountRowKey, messageSourceDetails);
		}
		// Lookup key not found for account, so create Lookup key first and then insert account record 
		else {	
			logger.debug("Account GUID does not exist in Lookup table - accountLookupRowKey: {} | accountGUID: {} | Message source: {}", accountLookupRowKey,  account.getId(), messageSourceDetails);
			
			String sha256hex = Hashing.sha256()
			  .hashString(accountLookupRowKey, StandardCharsets.UTF_8)
			  .toString();
			account.setId(sha256hex);
			
			bigtableClientUtil.updateCache(accountLookupRowKey, account.getId());
			
			// Insert Lookup Row
			String accountJsonString = gson.toJson(account, UberAccount.class);
			bigtableClientUtil.writeLookupTableRow(accountLookupRowKey,account.getId());
			logger.debug("Inserted account lookup record with RowKey: {} | Message Source: {} | File: {}", accountLookupRowKey,
					messageSourceDetails,accountDetail.getTags().get(FILE_NAME));

			// Insert Main Table Row
			String accountRowKey = BigtableUtil.buildAccountRowKey(account);
			bigtableClientUtil.writeMainTableRow(accountRowKey, accountJsonString, tagsJsonString);
			logger.debug("Inserted account record with RowKey: {} | Message Source: {} | File: {}", accountRowKey, messageSourceDetails,
					accountDetail.getTags().get(FILE_NAME));
		}
	}

}
