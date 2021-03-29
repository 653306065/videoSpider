package com.spider;

import com.spider.web.Pixiv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Date;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext ConfigurableApplicationContext= SpringApplication.run(Application.class);
        Pixiv Pixiv=ConfigurableApplicationContext.getBean(Pixiv.class);
        Pixiv.downloadImage();
    }
}
