package io.github.streamingpipeline.exception;

import io.github.streamingpipeline.utils.ErrorCode;
import io.github.streamingpipeline.utils.SeverityLevel;

/***
 * 
 * @author SravanVedala
 *
 */
public class UberAccountsPipelineException extends RuntimeException {
  
	private static final long serialVersionUID = 1L;
	private SeverityLevel severityLevel;
    private ErrorCode code;

    public UberAccountsPipelineException() {
        super();
    }

    public UberAccountsPipelineException(String message) {
        super(message);
    }

    public UberAccountsPipelineException(String message, ErrorCode code) {
    	super(message);
        this.code = code;
    }

    public UberAccountsPipelineException(String message, ErrorCode code, SeverityLevel severityLevel) {
        super(message);
        this.severityLevel = severityLevel;
        this.code = code;
    }

    public UberAccountsPipelineException(String message, Throwable cause, ErrorCode code) {
        super(message, cause);
        this.severityLevel = severityLevel;
        this.code = code;
    }
    
    public UberAccountsPipelineException(String message, ErrorCode code, SeverityLevel severityLevel, Throwable cause) {
        super(message, cause);
        this.severityLevel = severityLevel;
        this.code = code;
    }

    public UberAccountsPipelineException(Throwable cause) {
        super(cause);
        this.severityLevel = SeverityLevel.FATAL;
    }

    protected UberAccountsPipelineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SeverityLevel getSeverityLevel() {
        return severityLevel;
    }

    public UberAccountsPipelineException setSeverityLevel(SeverityLevel severityLevel) {
        this.severityLevel = severityLevel;
        return this;
    }

    public ErrorCode getCode() {
        return code;
    }

    public UberAccountsPipelineException setCode(ErrorCode code) {
        this.code = code;
        return this;
    }
}
