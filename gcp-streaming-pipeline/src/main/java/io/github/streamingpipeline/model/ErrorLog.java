package io.github.streamingpipeline.model;

import io.github.streamingpipeline.utils.SeverityLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/***
 * 
 * @author SravanVedala
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter 
@Setter
@Builder
public class ErrorLog {
	private String fileLogId;
	private String batchLogId;
	private SeverityLevel severityLevel;
	private String errorLogId;
	private String errorMessage;
	private String recordNum;
}
