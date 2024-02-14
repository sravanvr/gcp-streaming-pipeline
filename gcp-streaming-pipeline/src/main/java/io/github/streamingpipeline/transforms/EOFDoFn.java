package io.github.streamingpipeline.transforms;

import com.google.bigtable.v2.Mutation;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import io.github.streamingpipeline.TxnstBigtableOptions;
import io.github.streamingpipeline.config.ApplicationProperties;
import io.github.streamingpipeline.config.GuiceModule;
import io.github.streamingpipeline.service.EOFMessageProcessor;
import io.github.streamingpipeline.utils.BigtableUtil;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/***
 * 
 * @author Sravan Vedala
 *
 */
public class EOFDoFn extends DoFn<String, KV<ByteString, Iterable<Mutation>>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(EOFDoFn.class);
	private final Map<String, TupleTag<KV<ByteString, Iterable<Mutation>>>> tupleTagsMap;
	boolean guiceModuleInitialized = false;

	@Inject
	private transient EOFMessageProcessor eofMessageProcessor;

	@Inject
	private transient BigtableUtil bigtableUtil;

	public EOFDoFn(Map<String, TupleTag<KV<ByteString, Iterable<Mutation>>>> tupleTagsMap) {
		this.tupleTagsMap = tupleTagsMap;
	}
	
	@Teardown
	public void teardown() {
		LOGGER.debug("Teardown called");
		try {
			bigtableUtil.closeConnection();
			LOGGER.info("Bigtable connection has been closed");
		} catch (IOException e) {
			LOGGER.error("Exception during TearDown call - {}", e.getMessage());
		}
	}
	
	/***
	 * Keeping your Processor as a member variable and by initializing it in either "@Setup" method or in Process method,
	 * will provide a clean solution. 
	 * NOTE: You will end up having same number of "AccountProcessor" instances as that of number of DoFn instances Beam spawned for you.
	 * You have no control on how many parallel DoFn instances gets spawned by Beam. In small scale tests we observed 16 DoFn instances
	 * spawned on 3 worker machines.   
	 */
	@ProcessElement
	public void process(ProcessContext c, PipelineOptions po) {
		var pipelineOptions = c.getPipelineOptions().as(TxnstBigtableOptions.class);
		var applicationProperties = new ApplicationProperties();
		applicationProperties.setPipelineOptions(pipelineOptions);
		applicationProperties.setTupleTagsMap(this.tupleTagsMap);

		if (! guiceModuleInitialized) {
			LOGGER.info("Guice module initialized");
			var injector = Guice.createInjector(new GuiceModule(applicationProperties));
			injector.injectMembers(this);
			guiceModuleInitialized = true;
		}
		
		LOGGER.info("Accounts block received, calling AccountsProcessor");
		eofMessageProcessor.processEOF(c, pipelineOptions);
	}
}
