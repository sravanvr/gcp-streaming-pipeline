package io.github.streamingpipeline.service;

import java.util.Map;

import io.github.streamingpipeline.model.EOFMessage;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.inject.Inject;
import io.github.streamingpipeline.exception.UberAccountsPipelineException;
import io.github.streamingpipeline.model.UberAccountDetail;
import io.github.streamingpipeline.utils.ErrorCode;
import io.github.streamingpipeline.utils.MessageTags;

/***
 * 
 * @author Sravan Vedala
 *
 */
public class ErrorMessageBuilder {

	@Inject
	ImportManager importManager;
	
	private static final Logger logger = LoggerFactory.getLogger(ErrorMessageBuilder.class);

	// Build detailed error message from a given exception so that it can be used for logging into Log4J log.
	public String getErrorMessage(UberAccountsPipelineException ape, UberAccountDetail accountDetail) {
		return (buildErrorMessage(null, ape, ape.getMessage(), ape.getCause(), ape.getCode(), accountDetail));
	}
	
	// Build errorMessage that can be used by the caller to throw AccountsPipelineException.
	public String getErrorMessage(String errorMessage, ErrorCode errorCode, UberAccountDetail accountDetail) {
		return (buildErrorMessage(null, null, errorMessage, null, errorCode, accountDetail));
	}
	
	// Build detailed error message from a given RUNTIME unchecked exception so that it can be used for logging into Log4J log.
	public String getErrorMessage(Exception ex, UberAccountDetail accountDetail) {
		return (buildErrorMessage(null, ex, ex.getMessage(), ex.getCause(), ErrorCode.RUNTIME_EXCEPTION, accountDetail));
	}
		
	/**
	 * This is the central errorMessage builder when you have a non null AccountDetail object.
	 * Build detailed error message for logging in case of fatal exceptions.
	 * @param errorLabel
	 * @param errMessage
	 * @param cause
	 * @param errorCode
	 * @param accountDetail
	 * @return
	 */
	private String buildErrorMessage(String errorLabel, Exception exception, String errMessage, Throwable cause, ErrorCode errorCode, UberAccountDetail accountDetail) {
		
		StringBuilder builder = new StringBuilder();
		
		String errorDetailsPart = buildErrorDetailsPart(errorLabel, errMessage, cause, errorCode, accountDetail);
		
		builder
		.append(errorDetailsPart)
		.append(" - Message Identification - [\"")
		.append(accountDetail.getRecord())
		.append("]\"")
		.append(" - Message Source - [\"")
		.append(getMessageSourceDetails(accountDetail))
		.append("]\"");
		
		return (builder.toString());
	}
	
	private String buildErrorDetailsPart(String errorLabel, String errMessage, Throwable cause, ErrorCode errorCode, UberAccountDetail accountDetail) {

		StringBuilder builder = new StringBuilder();
		
		Map<String, String> tags = accountDetail.getTags();

		if (StringUtils.isNotBlank(errorLabel)) {
			builder.append(errorLabel)
			.append(" - ");
		} 

		// Add errorCode ENUM name as the prefix label to improve search.
		// Error-label-prefix example: "REQUIRED_FIELD_MISSING: Institution Configuration...."
		if (ObjectUtils.isNotEmpty(errorCode)) {
			builder
			.append(errorCode.name() + ": ");	
		}
		builder
		.append(errMessage);

		if (ObjectUtils.isNotEmpty(cause)) {
			builder.append(" - [Error cause: \"")
			.append(cause.getMessage())
			.append("\"] ")	;
		}

//		if (ObjectUtils.isNotEmpty(errorCode)) {
//			builder.append(" - [Error Code: \"")
//			.append(errorCode.getValue())
//			.append("\"] ")	;
//		}
		
		return (builder.toString());
	}
	
	// Use this when AccountDetail record is NULL at the time of Exception, and you want to use a portion of pubsubMessage for error record identification  
	public String buildErrorMessage(String errMessage, Throwable cause, ErrorCode errorCode, String pubsubMessage) {

		StringBuilder builder = new StringBuilder();

		builder
		.append(errorCode.name() + ": ");
		
		if (ObjectUtils.isNotEmpty(cause)) {
			builder.append(" - [Error cause: \"")
			.append(cause.getMessage())
			.append("\"] ")	;
		}

		if (ObjectUtils.isNotEmpty(errorCode)) {
			builder.append(" - [Error Code: \"")
			.append(errorCode.getValue())
			.append("\"] ")	;
		}
		
		builder
		.append(" - [Error Message: \"")
		.append(errMessage)	
		.append("\"]")
		.append(" - Message Identification - [\"");

		builder
		.append(pubsubMessage)
		.append(" \" ]");
		
		return (builder.toString());
	}
	
	/**
	 * 
	 * @return A string that gives details of message source, fileLogId, batchLogId, fileName (or batchId).
	 */
	public String getMessageSourceDetails (UberAccountDetail accountDetail) {
		var builder = new StringBuilder();
		if (ObjectUtils.isNotEmpty(accountDetail) && ObjectUtils.isNotEmpty(accountDetail.getTags())) {
			var tags = accountDetail.getTags();
			builder
					.append(" - [ Institution Id : \"")
					.append(importManager.extractInstitutionId(accountDetail));
			var batchId = tags.get(MessageTags.BATCH_ID_OR_FILE_NAME.getStringValue());
			if (StringUtils.isNotBlank(batchId)) {
				builder
						.append("\" | Batch Id (Or FileName): \"")
						.append(batchId);
			}
			var fileLogId = tags.get(MessageTags.FILE_LOG_ID.getStringValue());
			if (StringUtils.isNotBlank(fileLogId)) {
				builder
						.append("\" | FileLogId: \"")
						.append(fileLogId);
			}
			var batchLogId = tags.get(MessageTags.BATCH_LOG_ID.getStringValue());
			if (StringUtils.isNotBlank(batchLogId)) {
				builder
						.append("\" | BatchLogId: \"")
						.append(batchLogId);
			}
			var importId = tags.get(MessageTags.IMPORT_ID.getStringValue());
			if (StringUtils.isNotBlank(importId)) {
				builder
						.append("\" | ImportId: \"")
						.append(importId);
			}
			var source = tags.get(MessageTags.MESSAGE_SOURCE.getStringValue());
			if (StringUtils.isNotBlank(importId)) {
				builder
						.append("\" | Source: \"")
						.append(source);
			}
			builder.append("\" ]");
			var lineNumber = tags.get(MessageTags.IMPORT_RECORD_LINE_NUMBER.getStringValue());
			var blockNumber = tags.get(MessageTags.BLOCK_NUMBER.getStringValue());
			if (StringUtils.isNotBlank(lineNumber) && StringUtils.isNotBlank(blockNumber)) {
				builder
						.append(" - [ Line Number: \"")
						.append(lineNumber)
						.append("\" | Block Number: \"")
						.append(blockNumber)
						.append("\" ]");
			}
		}
		return (builder.toString());
	}

	/**
	 *
	 * @return A string that gives details of message source, fileLogId, batchLogId, fileName (or batchId).
	 */
	public String getEOFMessageSourceDetails(EOFMessage eofMessage) {
		var builder = new StringBuilder();
		if (ObjectUtils.isNotEmpty(eofMessage)) {
			builder
					.append(" - [Institution Id : \"")
					.append(eofMessage.getInstitutionId())
					.append("\" | FileLogId: \"")
					.append(eofMessage.getFileLogId())
					.append("\" | ImportId: \"")
					.append(eofMessage.getImportId())
					.append("\" ]");
		}
		return (builder.toString());
	}
}
