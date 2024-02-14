package io.github.streamingpipeline.service;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import com.google.inject.Inject;
import io.github.streamingpipeline.config.ApplicationProperties;


/***
 * 
 * @author SravanVedala
 *
 */
public class HttpClient {

	private ApplicationProperties properties;
	
	private RestTemplate restTemplate;
    
	private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
	
	@Inject
    public HttpClient(ApplicationProperties properties) {
		this.properties = properties;
    	restTemplate = new RestTemplate(getClientHttpRequestFactory());
    }
    
    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        int timeout = properties.getPipelineOptions().getHttpConnectionTimeout().get(); 
        int maxConnections = properties.getPipelineOptions().getHttpMaxConnections().get();
        
        RequestConfig config = RequestConfig.custom()
          .setConnectTimeout(timeout)
          .setConnectionRequestTimeout(timeout)
          .setSocketTimeout(timeout)
          .build();
        
        CloseableHttpClient client = HttpClientBuilder
          .create()
          .setKeepAliveStrategy(getConnectionKeepAliveStrategy())
          .setRetryHandler(retryHandler())
          .addInterceptorLast(getHttpResponseInterceptor())
          .setDefaultRequestConfig(config)
          .setMaxConnTotal(maxConnections) 
          .build();
        
        return new HttpComponentsClientHttpRequestFactory(client);
    }
	
	public RestTemplate getRestTemplate() {
		return restTemplate;
	}
	
	private HttpResponseInterceptor getHttpResponseInterceptor() {
		return new HttpResponseInterceptor() {
            @Override
            public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
                if (response.getStatusLine().getStatusCode() == 503) {
                    throw new IOException("Retry it");
                }
            }
        };
	}
	
	private ConnectionKeepAliveStrategy getConnectionKeepAliveStrategy() {
		 return new ConnectionKeepAliveStrategy() {
	            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
	                // Honor 'keep-alive' header
	                HeaderElementIterator it = new BasicHeaderElementIterator(
	                        response.headerIterator(HTTP.CONN_KEEP_ALIVE));
	                while (it.hasNext()) {
	                    HeaderElement he = it.nextElement();
	                    String param = he.getName();
	                    String value = he.getValue();
	                    if (value != null && param.equalsIgnoreCase("timeout")) {
	                        try {
	                            return Long.parseLong(value) * 1000;
	                        } catch(NumberFormatException ignore) {
	                        }
	                    }
	                }
	                HttpHost target = (HttpHost) context.getAttribute(
	                        HttpClientContext.HTTP_TARGET_HOST);
	                if (properties.getPipelineOptions().getUberChefUrl().get().contains(target.getHostName())) {
	                    // Keep alive for 5 seconds only
	                    return (long) 5 * 1000;
	                } else {
	                    // otherwise keep alive for 30 seconds
	                    return (long) 30 * 1000;
	                }
	            }
	        };
	}
	
	private HttpRequestRetryHandler retryHandler(){
        return (exception, executionCount, context) -> {

            logger.info("Retrying REST call due to exception-{}, attempt-#{}: ", exception, executionCount);

            if (executionCount >= 3) {
                // Do not retry if over max retry count
                return false;
            }
            if (exception instanceof java.net.SocketException) {
                // This is the main Connection reset exception we need to retry for.
                return false;
            }
            if (exception instanceof InterruptedIOException) {
                // Timeout
                return false;
            }
            if (exception instanceof UnknownHostException) {
                // Unknown host
                return false;
            }
            if (exception instanceof SSLException) {
                // SSL handshake exception
                return false;
            }

            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
            if (idempotent) {
                // Retry if the request is considered idempotent
                return true;
            }
            return false;
        };
    }
}