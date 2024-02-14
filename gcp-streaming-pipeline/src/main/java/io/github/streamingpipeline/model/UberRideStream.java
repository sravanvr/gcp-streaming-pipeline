package io.github.streamingpipeline.model;

import java.util.Date;
import java.util.UUID;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;

/***
 *
 * @author Sravan Vedala
 *
 */
@Data
public class UberRideStream {
    private UUID runstreamId;
	private String runstreamName;
	private String fileDirectoryName;
	private String runstreamVersion;
	private String processorNumber;
	private String processorName;
	private boolean isEndingWithCrlf;
	private boolean isFileIncremental;
	private boolean isIntradaySequenceEnabled;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS")
	private Date insertedDttm;
	private String insertedBy;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS")
	private Date updatedDttm;
	private String updatedBy;
}
