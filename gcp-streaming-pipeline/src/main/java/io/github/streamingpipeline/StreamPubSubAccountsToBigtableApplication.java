/*
 * Copyright (C) 2020 SecantLine Blogs. This material contains certain trade
 * secrets and confidential and proprietary information of SecantLine Blogs.
 * Use, reproduction, disclosure and distribution by any means are prohibited,
 * except pursuant to a written license from SecantLine Blogs. Use of copyright
 * notice is precautionary and does not imply publication or disclosure.
 */

package io.github.streamingpipeline;

import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TupleTag;
import com.google.bigtable.v2.Mutation;
import com.google.protobuf.ByteString;
import io.github.streamingpipeline.transforms.UberAccountToMutationsDoFn;
import io.github.streamingpipeline.transforms.EOFDoFn;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;

/**
 * The StreamPubSubAccountsToBigtableApplication pipeline is a streaming
 * pipeline which ingests data in JSon format from Cloud Pub/Sub, converts it to
 * Account Uber model object, captures key_columns and message body, and outputs
 * the resulting records as byte array into BigTable.
 */

public class StreamPubSubAccountsToBigtableApplication  {

	public static void main(String[] args) throws IOException {	
		TxnstBigtableOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(TxnstBigtableOptions.class);
		run(options);
	}

	public static PipelineResult run(TxnstBigtableOptions options) throws IOException {
		Pipeline pipeline = Pipeline.create(options);
		Map<String, TupleTag<KV<ByteString, Iterable<Mutation>>>> tupleTagMap = TupleTags.getTupleTagsMap();

		// Read string messages from a Pub/Sub topic.
		PCollection<PubsubMessage> pubsubMessages = pipeline
				.apply("Read PubSub Account Messages",
						PubsubIO.readMessagesWithAttributes().fromSubscription(options.getInputSubscription()));

		// Process regular data messages
		pubsubMessages
				.apply("Getting account data messages",
						Filter.by(input -> (Objects.isNull(input.getAttribute("eof"))))
				)
				// Get data messages payload strings
				.apply("Get account message strings",
						ParDo.of(new DoFn<PubsubMessage, String>() {
							@ProcessElement
							public void processElement(ProcessContext c) {
								c.output(new String(Objects.requireNonNull(c.element()).getPayload(),
										StandardCharsets.US_ASCII));
							}
						}))
				// Create Bigtable data and streaming lookup Mutations
				.apply("JsonString_To_Mutation",
						ParDo.of(new UberAccountToMutationsDoFn(tupleTagMap)));

		// Process EOF messages
		pubsubMessages
				.apply("Getting EOF messages",
						Filter.by(input -> (Objects.nonNull(input.getAttribute("eof"))))
				)
				// Get EOF messages payload strings
				.apply("Get EOF message strings",
						ParDo.of(new DoFn<PubsubMessage, String>() {
							@ProcessElement
							public void processElement(ProcessContext c) {
								c.output(new String(Objects.requireNonNull(c.element()).getPayload(),
										StandardCharsets.US_ASCII));
							}
						}))
				//
				// Step2: Process the EOF messages
				//
				.apply("EOFMessage_Processing",
						ParDo.of(new EOFDoFn(tupleTagMap)));
		return pipeline.run();
	}
}
