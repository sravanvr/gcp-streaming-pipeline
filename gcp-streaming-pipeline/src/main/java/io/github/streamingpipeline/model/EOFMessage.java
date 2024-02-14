package io.github.streamingpipeline.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/***
 * 
 * @author MichaelCarpenter
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EOFMessage {
	private UUID fileLogId;
	private String importId;
	private long recordCount;
	private OffsetDateTime fileTimestamp;
	private String institutionId;
	private int retryCount;
}
