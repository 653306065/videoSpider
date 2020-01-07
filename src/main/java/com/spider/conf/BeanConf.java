package com.spider.conf;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;

@Component
public class BeanConf {

	@Value("${proxy.host}")
	private String host;

	@Value("${proxy.port}")
	private int port;

	@Bean
	public YouTube createYouTube() {
		ApacheHttpTransport apacheHttpTransport = new ApacheHttpTransport.Builder().setProxy(new HttpHost(host, port)).build();
		YouTube Youtube = new YouTube.Builder(apacheHttpTransport, new JacksonFactory(), new HttpRequestInitializer() {
			public void initialize(HttpRequest request) throws IOException {

			}
		}).build();
		return Youtube;
	}
}
