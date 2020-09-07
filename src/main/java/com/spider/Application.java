package com.spider;

import com.alibaba.fastjson.JSON;
import com.spider.entity.Video;
import com.spider.web.Javrave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {

    static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class);
        Javrave Javrave = context.getBean(Javrave.class);
        Javrave.downloadUncensored();
    }
}
