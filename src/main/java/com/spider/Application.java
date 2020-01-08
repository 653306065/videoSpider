package com.spider;

import java.net.URL;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spider.web.Eporner;
import com.spider.web.Hanime;
import com.spider.web.Javfinder;
import com.spider.web.Pornhub;
import com.spider.web.YoutubeSpider;

@SpringBootApplication
public class Application {

	static Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(Application.class);
		YoutubeSpider YoutubeSpider=context.getBean(YoutubeSpider.class);
		String ChannelId= YoutubeSpider.getUserChannelId("aroundtheworld4k"); 
		YoutubeSpider.downloadByChannelId(ChannelId);
	}
}
