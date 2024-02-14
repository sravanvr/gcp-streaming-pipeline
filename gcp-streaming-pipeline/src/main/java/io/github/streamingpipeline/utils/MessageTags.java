package io.github.streamingpipeline.utils;

import java.util.Arrays;
import java.util.Optional;

/**
 * 
 * @author Sravan Vedala
 *
 */
public enum MessageTags {
	IMPORT_RECORD_LINE_NUMBER ("importRecordLineNumber"),
    IMPORT_ID ("importId"),
	BATCH_ID_OR_FILE_NAME ("batchId"),
	BLOCK_NUMBER ("block"),
	MESSAGE_SOURCE("source"),
	FILE_LOG_ID ("fileLogId"),
	BATCH_LOG_ID ("batchLogId");
		
	private final String text;
	
	MessageTags(String text) {
        this.text = text;
    }

    public String getStringValue() {
        return text;
    }
    
    public static Optional<MessageTags> fromValue(String text) {
        return Arrays.stream(values())
          .filter(messageTagValue -> messageTagValue.text.equals(text))
          .findFirst();
    }
}
