package com.spider.conf;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SystemConf {

    @PostConstruct
    public void init() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(availableProcessors * 2+1));
    }
}
