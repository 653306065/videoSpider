package com.spider;

import com.spider.web.Hanime;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext ConfigurableApplicationContext= SpringApplication.run(Application.class);
    }
}
