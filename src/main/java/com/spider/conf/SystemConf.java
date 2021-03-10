package com.spider.conf;

import org.springframework.beans.factory.annotation.Configurable;
import javax.annotation.PostConstruct;

@Configurable
public class SystemConf {

    @PostConstruct
    public void init() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(availableProcessors * 2 + 1));
    }
}
