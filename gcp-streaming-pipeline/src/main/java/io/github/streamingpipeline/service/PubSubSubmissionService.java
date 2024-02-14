package io.github.streamingpipeline.service;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import io.github.streamingpipeline.TxnstBigtableOptions;
import io.github.streamingpipeline.exception.ApplicationException;
import io.github.streamingpipeline.utils.ErrorCode;
import io.github.streamingpipeline.utils.SeverityLevel;
import org.apache.hadoop.hbase.shaded.org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PubSubSubmissionService {

    @Inject
    private GcpServiceFactory gcpServiceFactory;

    private final Logger logger = LoggerFactory.getLogger(PubSubSubmissionService.class);

    private final Map<String, Publisher> publishers = new ConcurrentHashMap<>();

    public String submitToRedirectorTopic(
            TxnstBigtableOptions pipelineOptions,
            String jsonBlock,
            Map<String, String> attributes
    ) {

        final var maxNumPubSubAttempts = pipelineOptions.getMaxNumPubSubAttempts().get();

        var topic = pipelineOptions.getPubsubRedirectorTopic().get();
        if (StringUtils.isBlank(topic)) {
            topic = pipelineOptions.getInputTopic().get();
        }

        var publisher = getPublisher(topic, maxNumPubSubAttempts);

        // Retry logic around submitting the message
        String messageId = null;
        var attemptCount = 0;
        Exception exception = null;
        while (attemptCount++ < maxNumPubSubAttempts) {
            try {
                messageId = publishMessage(publisher, jsonBlock, attributes);
                exception = null;
                break;
            } catch (InterruptedException e) {
                logger.warn("Pub/Sub submission was interrupted. Message = {}", e.getMessage());
                exception = e;
                Thread.currentThread().interrupt();
            } catch (ExecutionException | TimeoutException e) {
                logger.warn("Pub/Sub submission failed with message {}", e.getMessage());
                exception = e;
            }
        }
        if (null != exception) {
            var errorMessage =
                    "Pub/Sub submission failed. Max attempts reached. Error message: " + exception.getMessage();
            logger.error(errorMessage);
            throw new ApplicationException(
                    errorMessage,
                    ErrorCode.APPLICATION_EXCEPTION,
                    SeverityLevel.ERROR,
                    exception
            );
        } else {
            return messageId;
        }
    }

    public String publishMessage(Publisher publisher, String message, Map<String, String> attributes)
            throws ExecutionException, InterruptedException, TimeoutException {
        var data = ByteString.copyFromUtf8(message);
        var pubsubMessage = PubsubMessage.newBuilder().setData(data);
        if (null != attributes && ! attributes.isEmpty()) {
            pubsubMessage.putAllAttributes(attributes);
        }
        var messageIdFuture = publisher.publish(pubsubMessage.build());
        var messageId = messageIdFuture.get(10L, TimeUnit.SECONDS);
        logger.debug("Message Published! ID: {} Message: {}", messageId, message);
        logger.info("Message Published! ID: {}", messageId);
        return messageId;
    }

    private Publisher getPublisher(String topic, Integer maxAttempts) {
        if (null == this.publishers.get(topic)) {
            // Retry logic around getting the publisher
            var attemptCount = 0;
            IOException exception = null;
            while (attemptCount++ < maxAttempts) {
                try {
                    publishers.put(topic, gcpServiceFactory.getPublisher(topic));
                    exception = null;
                    break;
                } catch (IOException e) {
                    logger.warn("Pub/Sub connection failed with message {}", e.getMessage());
                    exception = e;
                }
            }
            if (null != exception) {
                var errorMessage =
                        "Pub/Sub connection failed. Max attempts reached. Error message: " + exception.getMessage();
                logger.error(errorMessage);
                throw new ApplicationException(
                        errorMessage,
                        ErrorCode.APPLICATION_EXCEPTION,
                        SeverityLevel.ERROR,
                        exception
                );
            }
        }
        return publishers.get(topic);
    }

}
