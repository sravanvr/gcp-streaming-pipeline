package io.github.streamingpipeline.config;

import java.util.Map;

import io.github.streamingpipeline.TxnstBigtableOptions;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;

import com.google.bigtable.v2.Mutation;
import com.google.protobuf.ByteString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author SravanVedala
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter 
@Setter
@Builder
public class ApplicationProperties {

	/**
	 * Generally all application properties come from Beam pipeline options class during runtime.
	 * However if there is a need for any additional custom properties, add them in this class and set those properties
	 * from any class that fits the bill. 
	 */
	
	/**
	 * Embed "BigtableOptions" object inside this class so that it will be bound to Guice module that gets instantiated
	 * in AccountProcessor class and consequently will become available for dependency injection from anywhere  
	 * in this application. 
	 */
	TxnstBigtableOptions pipelineOptions;
	
	Map<String, TupleTag<KV<ByteString, Iterable<Mutation>>>> tupleTagsMap;
}
