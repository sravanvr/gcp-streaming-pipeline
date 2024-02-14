package io.github.streamingpipeline.utils;

/***
 * 
 * @author rv250129 * 
 * This ENUM represents Validation Warnings
 */
public enum ErrorCode {

    RECORD_REJECTED(2000),
    REQUIRED_FIELD_MISSING(2001),
    INVALID_CONFIGURATION(2002),
    MESSAGE_VALIDATION_FAILED(2003),
    APPLICATION_EXCEPTION(2004),
    RUNTIME_EXCEPTION(2005),
    CONFIG_SERVER_UNAVAILABLE(2006),
    INTEGRATION_SERVICE_UNAVAILABLE(2007);
	
    private int value;
    private ErrorCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public String toString() {
        return Integer.toString(this.value);
    }
}
