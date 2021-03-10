package com.spider.conf;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.tomcat.util.threads.TaskQueue;
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

    @Value("${proxy.http.host}")
    private String host;

    @Value("${proxy.http.port}")
    private int port;

    @Bean
    public YouTube createYouTube() {
        ApacheHttpTransport apacheHttpTransport = new ApacheHttpTransport.Builder().setProxy(new HttpHost(host, port)).build();
        YouTube Youtube = new YouTube.Builder(apacheHttpTransport, new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {

            }
        }).build();
        return Youtube;
    }

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(8 * 3, 8 * 3 * 10, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(1000));
    }
}
