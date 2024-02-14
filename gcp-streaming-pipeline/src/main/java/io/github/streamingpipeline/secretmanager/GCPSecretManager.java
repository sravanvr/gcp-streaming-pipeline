package io.github.streamingpipeline.secretmanager;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.inject.Inject;
import io.github.streamingpipeline.config.ApplicationProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GCPSecretManager {

    private String projectId;
    private String secretId;

    @Inject
    public GCPSecretManager(ApplicationProperties applicationProperties) {
        this.projectId = applicationProperties.getPipelineOptions().getProject();
        this.secretId = applicationProperties.getPipelineOptions().getUberSecretId().get();
    }

    private static final Logger log = LoggerFactory.getLogger(GCPSecretManager.class);
    private static final String VERSION_ID_LATEST = "latest";

    public String fetchSecret() {
        if (StringUtils.isBlank(projectId) || StringUtils.isBlank(secretId)) {
            log.error("Missing project id or secret id, unable to fetch secret value");
            return "";
        }
        var secretValue = "";
        try (var client = SecretManagerServiceClient.create()) {
            var secretVersionName = SecretVersionName.of(projectId, secretId, VERSION_ID_LATEST);
            AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
            secretValue = response.getPayload().getData().toStringUtf8();
            log.debug("Successfully fetch secret value");
        } catch (Exception ex) {
            log.error("Error fetching secret value => {}", ex.getMessage());
        }
        return secretValue;
    }
}
