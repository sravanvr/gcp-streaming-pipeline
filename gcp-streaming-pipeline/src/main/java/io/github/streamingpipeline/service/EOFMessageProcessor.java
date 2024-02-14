package io.github.streamingpipeline.service;

import com.google.bigtable.v2.Mutation;
import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.models.Row;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.protobuf.ByteString;
import io.github.streamingpipeline.TxnstBigtableOptions;
import io.github.streamingpipeline.exception.ApplicationException;
import io.github.streamingpipeline.exception.UberAccountsPipelineException;
import io.github.streamingpipeline.model.EOFMessage;
import io.github.streamingpipeline.model.ErrorLog;
import io.github.streamingpipeline.utils.BigtableClientUtil;
import io.github.streamingpipeline.utils.ErrorCode;
import io.github.streamingpipeline.utils.ProcessStatus;
import io.github.streamingpipeline.utils.SeverityLevel;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class EOFMessageProcessor {

    @Inject
    BigtableClientUtil bigtableClientUtil;

    @Inject
    MetadataClient metadataClient;

    @Inject
    ImportManager importManager;

    @Inject
    @Named("getGson")
    Gson gson;

    @Inject
    private PubSubSubmissionService pubSubSubmissionService;

    @Inject
    ErrorMessageBuilder logMessageBuilder;

    private static final Logger LOGGER = LoggerFactory.getLogger(EOFMessageProcessor.class);

    /**
     * EOF Process Function
     * @param processContext ProcessContext
     * @throws UberAccountsPipelineException a runtime exception type
     */
    public void processEOF(
            DoFn<String, KV<ByteString, Iterable<Mutation>>>.ProcessContext processContext,
            TxnstBigtableOptions options
    ) throws UberAccountsPipelineException {
        List<ErrorLog> errorLogList = new ArrayList<>();

        var pubsubMessage = processContext.element();
        try {
            /* Get EOF message from pub-sub topic */
            var eofMessage = importManager.jsonStringToEOFMessage(pubsubMessage);
            LOGGER.info("End-of-File message received for importId = {} and importFileLogId = {}",
                    eofMessage.getImportId(), eofMessage.getFileLogId());

            if (eofMessage.getRetryCount() < options.getMaxEOFRetryAttempts().get()) {

                // check the counts and process the message
                if (countAndProcess(eofMessage, options)) {
                    LOGGER.info("EOF_PROCESSING_SUCCESS - EOF message for institutionId = {} and importId = {} has been successfully processed",
                            eofMessage.getInstitutionId(), eofMessage.getImportId());
                    var importFileLog = metadataClient.getFileLog(eofMessage.getFileLogId());
                    if (Objects.nonNull(importFileLog)) {
                        importFileLog.setProcessStatus(ProcessStatus.SUCCESSFUL);
                        metadataClient.updateFileLog(importFileLog);
                    } else {
                        LOGGER.error("Unable to update status to 'SUCCESSFUL' for importFileLog ID = {}",
                                eofMessage.getFileLogId());
                    }
                }
            } else {
                LOGGER.error("Giving up on End-of-File message for importId = {} and importFileLogId = {}. Too many retries have been attempted ({}).",
                        eofMessage.getImportId(), eofMessage.getFileLogId(), eofMessage.getRetryCount());
                var importFileLog = metadataClient.getFileLog(eofMessage.getFileLogId());
                if (Objects.nonNull(importFileLog)) {
                    importFileLog.setProcessStatus(ProcessStatus.FATAL);
                    metadataClient.updateFileLog(importFileLog);
                } else {
                    LOGGER.error("Unable to update status to 'FATAL' for importFileLog ID = {}",
                            eofMessage.getFileLogId());
                }
            }
        } catch (Exception e) {
            importManager.handleException(e, null, pubsubMessage, errorLogList);
        } finally {
            importManager.insertErrorLog(errorLogList);
        }
    }

    private boolean countAndProcess(EOFMessage eofMessage, TxnstBigtableOptions options) {
        try (var dataClient =
                     BigtableDataClient.create(options.getProject(), options.getBigtableInstanceId().get())) {

            // Validate all data has been persisted
            var bigtableRecordCount = bigtableClientUtil.countRecordsForBatch(
                    options, dataClient, eofMessage.getImportId(), eofMessage.getInstitutionId());
            if (eofMessage.getRecordCount() > bigtableRecordCount) {
                LOGGER.info("Expected record count {} does not match actual record count {}. Will wait and retry ({})...",
                        eofMessage.getRecordCount(), bigtableRecordCount, eofMessage.getRetryCount() + 1);
                // todo: verify whether we even need to use the redirector
                var delayed =
                        CompletableFuture.delayedExecutor(options.getEOFRetryDelaySeconds().get(), TimeUnit.SECONDS);
                CompletableFuture.supplyAsync(() -> resubmitEOFMessage(eofMessage, options), delayed);
                return false;
            } else {
                LOGGER.info("Expected record count {} is satisfied by actual record count {}. Creating lookup records ...",
                        eofMessage.getRecordCount(), bigtableRecordCount);
            }

            // Get the data records for the given importId
            var batchRecords = bigtableClientUtil.getRecordsForBatch(
                    options, dataClient, eofMessage.getImportId(), eofMessage.getInstitutionId());
            batchRecords.forEach(row ->
                    // process each account
                    processAccountForEOF(
                            eofMessage,
                            row)
            );
            return true;
        } catch (Exception e) {
            throw new ApplicationException(
                    "Exception occurred while trying to retrieve records for importId = " +
                            eofMessage.getImportId(),
                    ErrorCode.APPLICATION_EXCEPTION,
                    SeverityLevel.ERROR,
                    e
            );
        }
    }

    public String resubmitEOFMessage(EOFMessage eofMessage, TxnstBigtableOptions options) {
        // Publish the message to the redirector topic
        var attributes = new HashMap<String, String>();
        attributes.put("eof", "true");

        // increment the retryCount and publish the eofMessage
        eofMessage.setRetryCount(eofMessage.getRetryCount() + 1);
        LOGGER.info("Publishing EOF message = {}", eofMessage);
        var eofJson = gson.toJson(eofMessage);
        return pubSubSubmissionService.submitToRedirectorTopic(options, eofJson, attributes);
    }

    private void processAccountForEOF(
            EOFMessage eofMessage,
            Row row
    ) {
        var messageSourceDetails = logMessageBuilder.getEOFMessageSourceDetails(eofMessage);
        LOGGER.debug("EOF message : {}", messageSourceDetails);
        LOGGER.debug("Row = {}", row);

        // todo: fill in the processing here
    }

}
