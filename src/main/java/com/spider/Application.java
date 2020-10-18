package com.spider;

import com.alibaba.fastjson.JSON;
import com.spider.entity.Video;
import com.spider.service.VideoService;
import com.spider.web.Eporner;
import com.spider.web.Javbangers;
import com.spider.web.Javrave;
import com.spider.web.Pornhub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@SpringBootApplication
public class Application {

    static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class);
        Pornhub pornhub=context.getBean(Pornhub.class);
        pornhub.download_gangbang_hentai();
    }
}
