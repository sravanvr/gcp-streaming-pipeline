package io.github.streamingpipeline.transforms;

import java.util.Map;

import io.github.streamingpipeline.TxnstBigtableOptions;
import io.github.streamingpipeline.config.ApplicationProperties;
import io.github.streamingpipeline.config.GuiceModule;
import io.github.streamingpipeline.service.UberAccountProcessor;
import io.github.streamingpipeline.utils.BigtableClientUtil;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.bigtable.v2.Mutation;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.protobuf.ByteString;

/***
 * 
 * @author Sravan Vedala
 *
 */
@SuppressWarnings("serial")
public class UberAccountToMutationsDoFn extends DoFn<String, KV<ByteString, Iterable<Mutation>>> {
	
	private static final Logger logger = LoggerFactory.getLogger(UberAccountToMutationsDoFn.class);
	private Map<String, TupleTag<KV<ByteString, Iterable<Mutation>>>> tupleTagsMap;
	boolean guiceModuleInitialized = false;
	Injector injector;
	
	@Inject
	UberAccountProcessor accountProcessor;
		
	@Inject
	BigtableClientUtil bigtableClientUtil;
	
	public UberAccountToMutationsDoFn(Map<String, TupleTag<KV<ByteString, Iterable<Mutation>>>> tupleTagsMap) {
		this.tupleTagsMap = tupleTagsMap;
	}
	
	@Teardown
	public void teardown() {
		logger.debug("Teardown called");
		try {
			bigtableClientUtil.closeConnection();
			logger.info("Bigtable connection has been closed");
		} catch (Exception e) {
			logger.error("Exception during TearDown call - " + e.getMessage());
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
		TxnstBigtableOptions pipelineOptions = c.getPipelineOptions().as(TxnstBigtableOptions.class);
		ApplicationProperties applicationProperties = new ApplicationProperties();
		applicationProperties.setPipelineOptions(pipelineOptions);
		applicationProperties.setTupleTagsMap(this.tupleTagsMap);
		
		if (! guiceModuleInitialized) {
			logger.info("Guice module initialized");
			injector = Guice.createInjector(new GuiceModule(applicationProperties));
			injector.injectMembers(this);
			guiceModuleInitialized = true;
		}
		
		logger.info("Accounts block received, calling AccountsProcessor");
		accountProcessor.process(c);
	}
}