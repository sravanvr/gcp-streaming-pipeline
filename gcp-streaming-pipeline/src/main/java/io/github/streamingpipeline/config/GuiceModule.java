package io.github.streamingpipeline.config;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import io.github.streamingpipeline.secretmanager.GCPSecretManager;
import io.github.streamingpipeline.rowkey.UberAccountLookupRowKey;
import io.github.streamingpipeline.service.*;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import io.github.streamingpipeline.utils.BigtableClientUtil;

/**
 * 
 * @author SravanVedala
 *
 */
public class GuiceModule extends AbstractModule {

	private final ApplicationProperties applicationProperties;

	public GuiceModule(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

	@Override
	protected void configure() {
		bind(UberAccountProcessor.class).in(Scopes.SINGLETON);
		bind(ApplicationProperties.class).toInstance(applicationProperties);
		// Service classes
		bind(HttpClient.class).in(Scopes.SINGLETON);
		bind(MetadataClient.class).asEagerSingleton();
		bind(BigtableClientUtil.class).in(Scopes.SINGLETON);
		bind(UberAccountLookupRowKey.class).in(Scopes.SINGLETON);
		bind(ErrorMessageBuilder.class).in(Scopes.SINGLETON);
		bind(ImportManager.class).in(Scopes.SINGLETON);
		bind(String.class)
		.annotatedWith(Names.named("chiefUrl"))
		.toInstance(applicationProperties.getPipelineOptions().getChiefUrl().get());
		bind(UberConfigClient.class).in(Scopes.SINGLETON);
		bind(GCPSecretManager.class).in(Scopes.SINGLETON);
	}

	@Provides 
	@Singleton
	@Named("restTemplate")
	public RestTemplate getRestTemplate(HttpClient httpClient){
		return httpClient.getRestTemplate();
	}
	
	@Provides 
	@Singleton
	@Named("getGson")
	public Gson getGson() {
		return new GsonBuilder()
				.serializeNulls()
				.registerTypeAdapter(OffsetDateTime.class,
						(JsonSerializer<OffsetDateTime>) (localDate, type, context)
						-> new JsonPrimitive(localDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
				.registerTypeAdapter(OffsetDateTime.class,
						(JsonDeserializer<OffsetDateTime>) (jsonElement, type, context)
						-> OffsetDateTime.parse(jsonElement.getAsString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME))
				.create();
	}
	
	@Provides 
	@Singleton
	@Named("uberConfigClientImpl")
	public UberConfig getUberConfigClient(ApplicationProperties applicationProperties){
		String insBaseURL = applicationProperties.getPipelineOptions().getUberConfigInsBaseURL().get();
		String configServerBaseURL = applicationProperties.getPipelineOptions().getUberConfigServerBaseURL().get();
		String gcpProjectId = applicationProperties.getPipelineOptions().getProject();
		Long configCacheDuration = applicationProperties.getPipelineOptions().getUberConfigCacheDurationMinutes().get();
		Long insCacheDuration = applicationProperties.getPipelineOptions().getUberConfigInsCacheDurationMinutes().get();
		return new UberConfig();
	}
}
