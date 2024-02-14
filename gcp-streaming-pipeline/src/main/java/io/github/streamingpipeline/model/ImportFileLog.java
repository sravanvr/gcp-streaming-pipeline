package io.github.streamingpipeline.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import io.github.streamingpipeline.utils.ProcessStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author Sravan Vedala
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter 
@Setter
@Builder
public class ImportFileLog {
	private UUID fileLogId;
	private Integer dataTypeId;
	
	//private Integer processStatus;
	private ProcessStatus processStatus;
	
	private String fileName;
	private Long fileSize;
	private OffsetDateTime fileLastModifiedDttm;
	private OffsetDateTime processStartDttm;
	private OffsetDateTime processEndDttm;
	private String runstreamId;
	private String importSequence;
	private OffsetDateTime filePostDate;
	private Integer batchCount;
}
