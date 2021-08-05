package com.spider.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class SystemConf {

    @PostConstruct
    public void init() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(availableProcessors * 2 + 1));
    }

    @Bean("scheduleThreadPool")
    public ThreadPoolExecutor threadPoolExecutor(){
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        AtomicInteger i= new AtomicInteger();
        return new ThreadPoolExecutor(availableProcessors, availableProcessors * 10, 1, TimeUnit.MINUTES, new LinkedBlockingDeque<>(), r -> {
            Thread thread=new Thread(r);
            thread.setName("scheduleThread-"+i.get());
            i.getAndIncrement();
            return thread;
        },new ThreadPoolExecutor.DiscardOldestPolicy());
    }
}
