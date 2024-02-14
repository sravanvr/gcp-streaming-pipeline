package io.github.streamingpipeline.utils;

import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import com.google.inject.Inject;
import io.github.streamingpipeline.config.ApplicationProperties;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;

import java.io.IOException;

public class BigtableUtil {

	private static Connection connection;
	ApplicationProperties properties;
	
	@Inject
	public BigtableUtil(ApplicationProperties properties) {
		String projectId = properties.getPipelineOptions().getProject();
		String instanceId = properties.getPipelineOptions().getBigtableInstanceId().get();
		this.properties = properties;
		synchronized(BigtableUtil.class) {
			if (null == connection) {
				connection = createBigtableConnection(projectId, instanceId);
			}
		}
	}
	
	/**
	 * HBase Guidelines:
	 * Connection: Connection is a heavy-weight object and thread-safe, create one connection per JVM and don't close it.
	 * Table: Table is a lightweight object and not thread-safe, every thread should obtain its own Table instance.   
	 * 
	 */
	private Connection createBigtableConnection(String bigtableProjectId, String bigtableInstanceId) {
		Configuration configuration = BigtableConfiguration.configure(bigtableProjectId, bigtableInstanceId, "transaction-store-read-writes");
		configuration.set("google.bigtable.use.cached.data.channel.pool", "true");
		return BigtableConfiguration.connect(configuration);
	}
	
	public void closeConnection() throws IOException {
		synchronized(BigtableUtil.class) {
			if (null != connection) {
				connection.close();
				connection = null;
			}
		}
	}

}
