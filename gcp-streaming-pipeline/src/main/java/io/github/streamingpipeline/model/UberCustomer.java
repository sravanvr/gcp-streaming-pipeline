package io.github.streamingpipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/***
 * 
 * @author Sravan Vedala
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UberCustomer {

	@JsonProperty("institutionId")
	private String institutionId;

	@JsonProperty("rtn")
	private String rtn;

	@JsonProperty("cutoffDate")
	private String cutoffDate;

	@JsonProperty("enableNexus")
	private String enableNexus;
}
