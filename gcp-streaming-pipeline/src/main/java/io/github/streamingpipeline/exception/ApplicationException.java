package io.github.streamingpipeline.exception;

import io.github.streamingpipeline.utils.ErrorCode;
import io.github.streamingpipeline.utils.SeverityLevel;

/***
 * 
 * @author SravanVedala
 *
 */
public class ApplicationException extends UberAccountsPipelineException {
  
	private static final long serialVersionUID = 1L;

	public ApplicationException(String message, ErrorCode code) {
        super(message, code);
    }

    public ApplicationException(String message, ErrorCode code, SeverityLevel severityLevel) {
        super(message, code, severityLevel);
    }

    public ApplicationException(String message, ErrorCode code, SeverityLevel severityLevel, Throwable cause) {
        super(message, code, severityLevel, cause);
    }

    public ApplicationException(Throwable cause) {
        super(cause);
    }

    protected ApplicationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
