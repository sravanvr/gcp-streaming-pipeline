package io.github.streamingpipeline.exception;

import io.github.streamingpipeline.utils.ErrorCode;
import io.github.streamingpipeline.utils.SeverityLevel;

/***
 * 
 * @author SravanVedala
 *
 */
public class AccountsPipelineException extends RuntimeException {
  
	private static final long serialVersionUID = 1L;
	private SeverityLevel severityLevel;
    private ErrorCode code;

    public AccountsPipelineException() {
        super();
    }

    public AccountsPipelineException(String message) {
        super(message);
    }

    public AccountsPipelineException(String message, ErrorCode code) {
    	super(message);
        this.code = code;
    }

    public AccountsPipelineException(String message, ErrorCode code, SeverityLevel severityLevel) {
        super(message);
        this.severityLevel = severityLevel;
        this.code = code;
    }

    public AccountsPipelineException(String message, Throwable cause, ErrorCode code) {
        super(message, cause);
        this.severityLevel = severityLevel;
        this.code = code;
    }
    
    public AccountsPipelineException(String message, ErrorCode code, SeverityLevel severityLevel, Throwable cause) {
        super(message, cause);
        this.severityLevel = severityLevel;
        this.code = code;
    }

    public AccountsPipelineException(Throwable cause) {
        super(cause);
        this.severityLevel = SeverityLevel.FATAL;
    }

    protected AccountsPipelineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SeverityLevel getSeverityLevel() {
        return severityLevel;
    }

    public AccountsPipelineException setSeverityLevel(SeverityLevel severityLevel) {
        this.severityLevel = severityLevel;
        return this;
    }

    public ErrorCode getCode() {
        return code;
    }

    public AccountsPipelineException setCode(ErrorCode code) {
        this.code = code;
        return this;
    }
}
