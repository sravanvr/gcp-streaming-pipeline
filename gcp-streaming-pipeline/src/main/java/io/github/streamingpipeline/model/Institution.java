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
public class Institution {

	@JsonProperty("institutionId")
	private String institutionId;

	@JsonProperty("rtn")
	private String rtn;

	@JsonProperty("fiFriendlyName")
	private String fiFriendlyName;

	@JsonProperty("fiLegalName")
	private String fiLegalName;

	@JsonProperty("customerIdentifierFields")
	private String customerIdentifierFields;

	@JsonProperty("accountIdentifierFields")
	private String accountIdentifierFields;

	@JsonProperty("transactionIdentifierFields")
	private String transactionIdentifierFields;

	@JsonProperty("transferIdentifierFields")
	private String transferIdentifierFields;

	@JsonProperty("customerHostIdTypes")
	private String customerHostIdTypes;

	@JsonProperty("accountHostIdTypes")
	private String accountHostIdTypes;

	@JsonProperty("accountHostAccountIdTypes")
	private String accountHostAccountIdTypes;

	@JsonProperty("transactionHostAccountIdTypes")
	private String transactionHostAccountIdTypes;

	@JsonProperty("cutoffDate")
	private String cutoffDate;

	@JsonProperty("enableNexus")
	private String enableNexus;
}
