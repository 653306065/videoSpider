package com.spider.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import javax.annotation.PostConstruct;
import java.net.Proxy;

public abstract class BaseWeb {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected Proxy proxy;

    @Value("${proxy.enabled}")
    private boolean enabled;

    @PostConstruct
    public void initialize(){
        if(!enableProxy()){
            proxy=null;
        }
    }

    public abstract boolean enableProxy();

    protected final long  defaultSegmentSize=1024*1024*5;
}
