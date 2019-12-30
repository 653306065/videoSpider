package com.spider;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.spider.web.Hanime;
import com.spider.web.Javfinder;
import com.spider.web.Pornhub;

@SpringBootApplication
public class Application {

	static Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(Application.class);
		Pornhub Pornhub = context.getBean(Pornhub.class);
		Pornhub.downloadHmv();
	}
}
