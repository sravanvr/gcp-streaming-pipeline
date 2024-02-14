package io.github.streamingpipeline.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.streamingpipeline.model.ErrorLog;
import io.github.streamingpipeline.model.UberImportFileLog;
import io.github.streamingpipeline.model.UberCustomer;
import io.github.streamingpipeline.model.UberRideStream;
import io.github.streamingpipeline.secretmanager.GCPSecretManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * 
 * @author SravanVedala
 *
 */
public class MetadataClient {
	
	@Inject
	@Named ("restTemplate")
	RestTemplate restTemplate;
		
	@Inject
	@Named ("chiefUrl")
	String ROOT_URL;

	@Inject
	GCPSecretManager gcpSecretManager;

	@Inject
	@Named ("getGson")
	com.google.gson.Gson gson;

	final String FILE_LOG_URL = "/uberimportfilelog";
	final String ERROR_LOG_URL = "/errorlog";
	final String RUNSTREAM_URL = "/raidstreams";
	final String RUNSTREAM_INSTITUTIONS_URL = "/raidstream-ubercustomer/ubercustomer";
	final String TS_INSTITUTIONS_URL = "/ubercustomer-config";
	public static final String HEADER_AUTHORIZATION = "Authorization";

	/**
	 * FIleLog repository client functions
	 */
	public UberImportFileLog getFileLog(UUID fileLogId) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HEADER_AUTHORIZATION, gcpSecretManager.fetchSecret());
		HttpEntity request = new HttpEntity(headers);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(ROOT_URL + FILE_LOG_URL)
                .queryParam("fileLogId", fileLogId);

		ResponseEntity<UberImportFileLog> response = restTemplate.exchange(
				uriBuilder.toUriString(),
				HttpMethod.GET,
				request,
				UberImportFileLog.class
		);
		return response.getBody();
	}
	
	public void updateFileLog(UberImportFileLog fileLog) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HEADER_AUTHORIZATION, gcpSecretManager.fetchSecret());
		HttpEntity request = new HttpEntity(headers);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(ROOT_URL + FILE_LOG_URL);

		HttpEntity<UberImportFileLog> requestEntity = new HttpEntity<UberImportFileLog>(fileLog, headers);

		HttpEntity<UberImportFileLog> response = restTemplate.exchange(
				uriBuilder.toUriString(),
				HttpMethod.PUT,
				requestEntity,
				UberImportFileLog.class);
	}
	
	public void updateFileLog(UUID fileLogId, UberImportFileLog fileLog, OffsetDateTime filePostDate, Integer processStatusId) {
		Map<String, Object> uriParam = new HashMap<>();
	    uriParam.put("fileLogId", fileLogId.toString());

		HttpHeaders headers = new HttpHeaders();
		headers.set(HEADER_AUTHORIZATION, gcpSecretManager.fetchSecret());
		HttpEntity request = new HttpEntity(headers);

		HttpEntity<UberImportFileLog> requestEntity = new HttpEntity<UberImportFileLog>(fileLog, headers);

	    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(ROOT_URL + FILE_LOG_URL + "/{fileLogId}");

		uriComponentsBuilder.uriVariables(uriParam);
	    		uriComponentsBuilder
	    		.queryParam("filePostDate", filePostDate)
				.queryParam("processStatusId", processStatusId)
	            .build()
	            .toUri();

		HttpEntity<UberImportFileLog> response = restTemplate.exchange(
				uriComponentsBuilder.toUriString(),
				HttpMethod.PUT,
				requestEntity,
				UberImportFileLog.class);
	}
	
	/**
	 * ErrorLog repository client functions
	 */
	
	public List<ErrorLog> createErrorLog(List<ErrorLog> errorLogList) {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(ROOT_URL + ERROR_LOG_URL);

		HttpHeaders headers = new HttpHeaders();
		headers.set(HEADER_AUTHORIZATION, gcpSecretManager.fetchSecret());
		HttpEntity<List<ErrorLog>> request = new HttpEntity<List<ErrorLog>>(errorLogList, headers);

		ResponseEntity<List<ErrorLog>> response = restTemplate.exchange(
				uriBuilder.toUriString(),
				HttpMethod.POST,
				request,
				new ParameterizedTypeReference<List<ErrorLog>>() {}
		);
		return response.getBody();
	}
	
	/**
	 * Run-stream configuration client
	 */
	
	public UberRideStream getRunstreamConfig(String runstreamId) {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(ROOT_URL + RUNSTREAM_URL)
				.queryParam("runstreamName", runstreamId);

		HttpHeaders headers = new HttpHeaders();
		headers.set(HEADER_AUTHORIZATION, gcpSecretManager.fetchSecret());
		HttpEntity request = new HttpEntity(headers);

		ResponseEntity<UberRideStream> response = restTemplate.exchange(
				uriBuilder.toUriString(),
				HttpMethod.GET,
				request,
				UberRideStream.class
		);

		return response.getBody();
	}
		
	/**
	 * Institution configuration client
	 */
	
	public List<UberCustomer> getRunStreamInstitutions(String runstreamId) {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(ROOT_URL + RUNSTREAM_INSTITUTIONS_URL)
				 .queryParam("runstreamName", runstreamId);

		HttpHeaders headers = new HttpHeaders();
		headers.set(HEADER_AUTHORIZATION, gcpSecretManager.fetchSecret());
		HttpEntity request = new HttpEntity(headers);

		ResponseEntity<List<UberCustomer>> response = restTemplate.exchange(
				uriBuilder.toUriString(),
				HttpMethod.GET,
				request,
				new ParameterizedTypeReference<List<UberCustomer>>() {}
		);
		return response.getBody();
	}
	
	public Optional<UberCustomer> getInstitutions(String institutionId) {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(ROOT_URL + TS_INSTITUTIONS_URL)
				 .queryParam("profile", institutionId);

		HttpHeaders headers = new HttpHeaders();
		headers.set(HEADER_AUTHORIZATION, gcpSecretManager.fetchSecret());
		HttpEntity request = new HttpEntity(headers);

		ResponseEntity<UberCustomer> response = restTemplate.exchange(
				uriBuilder.toUriString(),
				HttpMethod.GET,
				request,
				UberCustomer.class
		);
		return Optional.ofNullable(response.getBody());
	}
}