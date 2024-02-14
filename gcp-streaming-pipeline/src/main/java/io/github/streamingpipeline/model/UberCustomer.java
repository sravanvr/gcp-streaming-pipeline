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
	private String uberCustomerId;

	@JsonProperty("rtn")
	private String rtn;

	@JsonProperty("cutoffDate")
	private String cutoffDate;

	@JsonProperty("enableUberNetwork")
	private String enableUberNetwork;
}
