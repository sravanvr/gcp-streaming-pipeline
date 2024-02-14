package io.github.streamingpipeline.service;

import com.google.cloud.pubsub.v1.Publisher;

import java.io.IOException;

public class GcpServiceFactory {

    public Publisher getPublisher(String topicId) throws IOException {
        return Publisher.newBuilder(topicId).build();
    }

}
