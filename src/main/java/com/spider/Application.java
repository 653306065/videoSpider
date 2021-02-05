package com.spider;

import com.alibaba.fastjson.JSON;
import com.spider.entity.Video;
import com.spider.web.Hqporner;
import com.spider.web.Javfull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Objects;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class);
        Javfull Javfull = context.getBean(Javfull.class);
        Javfull.downloadByCategory("10musume");
    }
}
