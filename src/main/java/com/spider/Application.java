package com.spider;

import com.spider.entity.Video;
import com.spider.web.Hqporner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Objects;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {

        ConfigurableApplicationContext context= SpringApplication.run(Application.class);
        Hqporner Hqporner=context.getBean(Hqporner.class);
        Hqporner.download4k();
    }
}
