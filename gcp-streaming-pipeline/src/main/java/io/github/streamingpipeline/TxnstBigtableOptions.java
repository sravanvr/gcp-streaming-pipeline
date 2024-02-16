package io.github.streamingpipeline;

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.ValueProvider;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.options.Default;
/**
 * The {@link TxnstBigtableOptions} class provides the custom execution options passed by the executor at the
 * command-line.
 */

 public interface TxnstBigtableOptions extends DataflowPipelineOptions, PipelineOptions   {

		@Description("Google Cloud Bigtable instance ID.")
		@Default.String("streamingpipeline-instance-dev")
		ValueProvider<String> getBigtableInstanceId();
		void setBigtableInstanceId(ValueProvider<String> bigtableInstanceId);

		@Description("Cloud Bigtable table ID for Accounts.")
		@Default.String("streamingpipeline-bt-accounts-dev")
		ValueProvider<String> getAccountTableId();
		void setAccountTableId(ValueProvider<String> accountTableId);
		
		@Description("Cloud Bigtable table ID for Account-lookup.")
		@Default.String("streamingpipeline-lookup")
		ValueProvider<String> getLookupTableId();
		void setLookupTableId(ValueProvider<String> lookupTableId);

		@Description("The Cloud Pub/Sub topic to read from.")
		@Required
		@Default.String("projects/gcp-streaming-pipeline/uber-accounts-dev")
		ValueProvider<String> getInputTopic();
		void setInputTopic(ValueProvider<String> value);

		@Description(
				"The Cloud Pub/Sub subscription to consume from. "
						+ "The name should be in the format of "
						+ "projects/<project-id>/subscriptions/<subscription-name>.")
		@Default.String("projects/gcp-streaming-pipeline/uber-accounts-pubsub")
		ValueProvider<String> getInputSubscription();
		void setInputSubscription(ValueProvider<String> value);
		
		@Description("Uber-chef URL")
		@Default.String("http://localhost/uber-chef")
		ValueProvider<String> getUberChefUrl();
		void setUberChefUrl(ValueProvider<String> value);
		
		@Description("Http Connection timeout")
		@Default.Integer(60000)
		ValueProvider<Integer> getHttpConnectionTimeout();
		void setHttpConnectionTimeout(ValueProvider<Integer> value);
		
		@Description("Http Max Connections")
		@Default.Integer(100)
		ValueProvider<Integer> getHttpMaxConnections();
		void setHttpMaxConnections(ValueProvider<Integer> value);
		
		@Description("Uber Config Ins Base URL")
		@Default.String("http://uber-config.com")
		ValueProvider<String> getUberConfigInsBaseURL();
		void setUberConfigInsBaseURL(ValueProvider<String> value);
		
		@Description("Uber Config Server Base URL")
		@Default.String("http://uber-config.com")
		ValueProvider<String> getUberConfigServerBaseURL();
		void setUberConfigServerBaseURL(ValueProvider<String> value);
		
		@Description("Uber Config Cache Duration in Minutes")
		@Default.Long(60L)
		ValueProvider<Long> getUberConfigCacheDurationMinutes();
		void setUberConfigCacheDurationMinutes(ValueProvider<Long> value);
		
		@Description("Uber Config Ins Cache Duration in Minutes")
		@Default.Long(60L)
		ValueProvider<Long> getUberConfigInsCacheDurationMinutes();
		void setUberConfigInsCacheDurationMinutes(ValueProvider<Long> value);

		@Description("The Cloud Bigtable App Profile Id")
		@Default.String("transaction-store-read-writes")
		ValueProvider<String> getAppProfileId();
		void setAppProfileId(ValueProvider<String> value);

		@Description("GSM secret id for Uber services")
		@Default.String("uber-secrets")
		ValueProvider<String> getUberSecretId();
		void setUberSecretId(ValueProvider<String> value);

		@Description("EOF retry delay (seconds)")
		@Default.Long(60)
		ValueProvider<Long> getEOFRetryDelaySeconds();
		void setEOFRetryDelaySeconds(ValueProvider<Long> value);

		@Description("Maximum number of EOF retry attempts for any given EOF message")
		@Default.Integer(120)
		ValueProvider<Integer> getMaxEOFRetryAttempts();
		void setMaxEOFRetryAttempts(ValueProvider<Integer> value);

		@Description("Maximum number of PubSub attempts to be made for a single submission")
		@Default.Integer(4)
		ValueProvider<Integer> getMaxNumPubSubAttempts();
		void setMaxNumPubSubAttempts(ValueProvider<Integer> value);

		@Description("The Cloud Pub/Sub topic to redirect eof messages to when needed.")
		@Required
		@Default.String("projects/dbk-streamdata-global-nonprod/topics/dbk-ts-topic-accounts-dev")
		ValueProvider<String> getPubsubRedirectorTopic();
		void setPubsubRedirectorTopic(ValueProvider<String> value);
	}
