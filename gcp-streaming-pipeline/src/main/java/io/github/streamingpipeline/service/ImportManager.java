package io.github.streamingpipeline.service;

import com.google.gson.*;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.streamingpipeline.exception.UberAccountsPipelineException;
import io.github.streamingpipeline.exception.ApplicationException;
import io.github.streamingpipeline.model.UberAccountDetail;
import io.github.streamingpipeline.model.EOFMessage;
import io.github.streamingpipeline.model.ErrorLog;
import io.github.streamingpipeline.model.UberCustomer;
import io.github.streamingpipeline.utils.ErrorCode;
import io.github.streamingpipeline.utils.SeverityLevel;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.github.streamingpipeline.utils.Constants.BATCH_LOG_ID;
import static io.github.streamingpipeline.utils.Constants.FILE_LOG_ID;
import static io.github.streamingpipeline.utils.Constants.IMPORT_RECORD_LINE_NUMBER;

/***
 * 
 * @author Sravan Vedala
 *
 */
public class ImportManager {

	@Inject
	ErrorMessageBuilder errorMessageBuilder;
	
	@Inject
	MetadataClient metadataClient;
	
	@Inject
	@Named ("getGson")
	Gson gson;
	
	@Inject
	UberConfigClient uberConfigsClient;
	
	private static final Logger logger = LoggerFactory.getLogger(ImportManager.class);
	
	public String extractInstitutionId(UberAccountDetail accountDetail) {
		if (ObjectUtils.allNotNull(accountDetail, accountDetail.getRecord())) {
			return accountDetail.getRecord().getInstitutionId();	
		} else {
			return null;
		}
	}
	
	public String getInstitutionId(UberAccountDetail accountDetail) throws UberAccountsPipelineException {
		String institutionId = extractInstitutionId(accountDetail);
		if (StringUtils.isBlank(institutionId)) {			
			String errorMessage = errorMessageBuilder.getErrorMessage("InstitutionId is missing in Accounts pubsub message. Message not processed.", null, accountDetail);
			throw new UberAccountsPipelineException(errorMessage, ErrorCode.MESSAGE_VALIDATION_FAILED);
		}
		return institutionId;
	}
	
	public Gson getGson() {
		Gson gson = new GsonBuilder()
				.serializeNulls()
				.registerTypeAdapter(OffsetDateTime.class,
						(JsonSerializer<OffsetDateTime>) (localDate, type, context)
						-> new JsonPrimitive(localDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
				.registerTypeAdapter(OffsetDateTime.class,
						(JsonDeserializer<OffsetDateTime>) (jsonElement, type, context)
						-> OffsetDateTime.parse(jsonElement.getAsString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME))
				.create();
		return gson;
	}
	
	/**
	 * @return Institution configuration
	 * @throws UberAccountsPipelineException
	 */
	public UberCustomer getInstitutionConfig(UberAccountDetail accountDetail) throws UberAccountsPipelineException {
		String institutionId = getInstitutionId(accountDetail);
		Optional<UberCustomer> institution = Optional.empty();
		try {
			institution = uberConfigsClient.getInstitution(institutionId);
		} catch (NullPointerException npe) {
			throw new UberAccountsPipelineException("Institution configuration is not available for institutionId= \"" + institutionId + "\"", ErrorCode.INVALID_CONFIGURATION);
		} catch (ApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new UberAccountsPipelineException("Exception while getting Institution configuration for institutionId= \"" + institutionId + "\"", e, ErrorCode.INVALID_CONFIGURATION);
		}
		if (!institution.isPresent()) {
			throw new UberAccountsPipelineException("Institution configuration is not available for institutionId= \"" + institutionId + "\"", ErrorCode.INVALID_CONFIGURATION);
		}
		return institution.get();
	}

	/**
	 * 
	 * @param pubSubMessage
	 * @return JsonElement representation of pubSubMessage 
	 * @throws UberAccountsPipelineException
	 */
	public JsonElement validatePubSubMessage(String pubSubMessage) throws UberAccountsPipelineException {
		logger.debug("Accounts message block pulled into pipeline: {}", pubSubMessage);		
		if (StringUtils.isBlank(pubSubMessage)) {			
			throw new UberAccountsPipelineException("EMPTY_PUB_SUB_MESSAGE: Accounts pubsub message is empty. Message not processed.", ErrorCode.MESSAGE_VALIDATION_FAILED);
		}		
		JsonElement element = null;
		try {
			Gson gsn=getGson();
			element = gsn.fromJson(pubSubMessage, JsonElement.class);
		} catch (Exception e) {
			throw new UberAccountsPipelineException("INVALID_JSON: Pubsub Accounts message is not a valid Json object. Message not processed.", e, ErrorCode.MESSAGE_VALIDATION_FAILED);
		}
		if (! element.isJsonArray()) {				
			throw new UberAccountsPipelineException("NOT_A_JSON_ARRAY: Accounts pubsub message is not a JSon Array. Pubsub message must be a Json array in order to be processed by Save pipelines. Message not processed.", ErrorCode.MESSAGE_VALIDATION_FAILED);
		}
		JsonArray pubSub = element.getAsJsonArray();
		if (pubSub.size() <= 0) {
			throw new UberAccountsPipelineException("EMPTY_JSON_ARRAY: Accounts pubsub message is an empty JSon Array. Message not processed.", ErrorCode.MESSAGE_VALIDATION_FAILED);
		}
		return element;
	}
	
	public List<UberAccountDetail> jsonArrayToAccount(JsonArray array) {
		List<UberAccountDetail> s = new ArrayList<UberAccountDetail>();
		for (JsonElement element : array) {
			try {
				Gson gsn=getGson();
				UberAccountDetail account = gsn.fromJson(element, UberAccountDetail.class);
				if (account != null && account.getRecord() != null && account.getTags() != null) {
    				s.add(account);
    			} else {
    				logger.error("INVALID_MESSAGE : Invalid message identified while converting JSon array to Accounts array. It could be due to message being empty or missing message tags - Json Element: {}", element);
    			}
			} catch (Exception ex) {
				throw new UberAccountsPipelineException("JSON_TO_ACCOUNT_CONVERSION_FAILED: Failed to deserialize pubsub JSon block to list of Uber model objects of type Account. Message not processed. Failed Message: " + element, ex, ErrorCode.MESSAGE_VALIDATION_FAILED);
			}
		}

		if (s.isEmpty()) {
			throw new UberAccountsPipelineException("INVALID_MESSAGE: Invalid message identified while converting JSon array to Accounts array. Accounts array is empty.", ErrorCode.MESSAGE_VALIDATION_FAILED);
		}
		return s;
	}

	public String getTags(UberAccountDetail wrapper) {
		String tags = null;
		try {
			tags = gson.toJson(wrapper.getTags(), Map.class);	
		} catch (Exception e) {
			logger.error("Exception while getting tags in Account record for InstitutionId {} and AccountId {}. Continuing with rest of the records in the block. ", wrapper.getRecord().getInstitutionId(), wrapper.getRecord().getAccountId() + e.getMessage());
		}
		if (tags == null) {
			logger.error("Tags not available in Account record for InstitutionId {} and AccountId {}. Continuing with rest of the records in the block. ");
		}
		return tags;
	}
	
	// Use this when AccountDetail list is NULL at the time of Exception, and you want to use a portion of pubsubMessage for error record identification
	public void handleException(Exception e, List<UberAccountDetail> accounts, String pubsubMessage, List<ErrorLog> errorLogList) {
		String errorMessage = null;
		if (ObjectUtils.isNotEmpty(accounts)) {
			handleException(e, accounts.get(0), errorLogList);
		}  else {
			if (e instanceof UberAccountsPipelineException) {
				errorMessage = errorMessageBuilder.buildErrorMessage(e.getMessage(), e.getCause(), ((UberAccountsPipelineException) e).getCode(), pubsubMessage);
				logger.error("AccountPipeline Error - " + errorMessage);
			} else {
				errorMessage = errorMessageBuilder.buildErrorMessage(e.getMessage(), e.getCause(), ErrorCode.RUNTIME_EXCEPTION, pubsubMessage);
				logger.error("AccountPipeline Error - " + errorMessage);
			}
		}
	}
	
	// Central Exception Handler
	public void handleException(Exception e, UberAccountDetail accountDetail, List<ErrorLog> errorLogList) {
		String errorMessage = null;		
		if (e instanceof UberAccountsPipelineException) {
			errorMessage = errorMessageBuilder.getErrorMessage((UberAccountsPipelineException) e, accountDetail);
		} else {
			errorMessage = errorMessageBuilder.getErrorMessage(e, accountDetail);
		}
		
		logger.error("AccountPipeline Error - " + errorMessage + ". Processing will continue with next record");
		
		ErrorLog errorLog = getErrorLog(errorMessage, accountDetail);
		if (ObjectUtils.isNotEmpty(errorLog)) {
			errorLogList.add(errorLog);	
		}
	}
	
	public void insertErrorLog(List<ErrorLog> errorLogList) {
		if (! ObjectUtils.isEmpty(errorLogList)) {
			metadataClient.createErrorLog(errorLogList);	
		} else {
			logger.debug("No ErrorLog exist for current DoFn transform call. No action taken to insert Accounts pipeline errors into error_log table.");
		}
	}
	
	public ErrorLog getErrorLog(String errorMessage, UberAccountDetail accountDetail) {
		try {
			String fileLogId = accountDetail.getTags().get(FILE_LOG_ID);
			String batchLogId = accountDetail.getTags().get(BATCH_LOG_ID);
			if (StringUtils.isNotBlank(fileLogId) && StringUtils.isNotBlank(batchLogId)) {
				ErrorLog errorLog =  ErrorLog.builder()
						.fileLogId(fileLogId)
						.batchLogId(batchLogId)
						.errorMessage(errorMessage.substring(0, 255))
						.severityLevel(SeverityLevel.FATAL)
						.recordNum(accountDetail.getTags().get(IMPORT_RECORD_LINE_NUMBER))
						.build();
				return errorLog;	
			} else {
				logger.error("\"FileLogId\" AND/OR BankLogId cannot be found in AccountDetail tags. Cannot build ErrorLog object. Original error message created for error_log table : {}" , errorMessage);
				return null;
			}				
		} catch (Exception e) {
			logger.error("Error while creating errorLog object - " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * DEV NOTE: 
	 * Keep this function for testing purpose. Batch Importer should send both fileLogId and bankLogId which will be 
	 * used by "Save" data-flows to record fatal exceptions in Error_Log table. 
	 */
	public UUID getUUIDFromString(String batchId) throws DecoderException {
		byte[] data = batchId.getBytes();
		String uuid = new UUID(ByteBuffer.wrap(data, 0, 8).getLong(), ByteBuffer.wrap(data, 8, 8).getLong()).toString();
		return UUID.fromString(uuid);
	}

	public EOFMessage jsonStringToEOFMessage(String jsonString) {
		try {
			var eofMessage = gson.fromJson(jsonString, EOFMessage.class);
			if (null == eofMessage) {
				logger.error("INVALID_MESSAGE : Invalid message identified while converting message to EOFMessage object. It could be due to message being empty or missing message tags - Json String: {}",
						jsonString);
			}
			return eofMessage;
		} catch (Exception e) {
			throw new UberAccountsPipelineException("INVALID_JSON: Pubsub Accounts EOF message is not a valid Json object. Message not processed.",
					e, ErrorCode.MESSAGE_VALIDATION_FAILED);
		}
	}
}
