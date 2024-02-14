package io.github.streamingpipeline.service;

import java.util.Map;
import java.util.Optional;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.streamingpipeline.model.UberCustomer;

/**
 * 
 * @author Sravan Vedala
 *
 */
public class UberConfigClient {
	
	@Inject
	@Named ("uberConfigClientImpl")
    UberConfig uberConfigClientImpl;
	
	public Optional<UberCustomer> getInstitution(String profile) throws NullPointerException, Exception {
			Map<String, String> uberConfigMap = uberConfigClientImpl.getConfigurations(profile, "uber-chef");
			UberCustomer institution =
					UberCustomer.builder()
					.institutionId(profile)
					.rtn(uberConfigMap.get("rtn"))
					.build();
			return Optional.ofNullable(institution);
	}
}