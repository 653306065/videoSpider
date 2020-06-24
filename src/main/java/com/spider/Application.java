package com.spider;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spider.entity.By114BT;
import com.spider.utils.FFmpegUtil;
import com.spider.utils.FileUtils;
import com.spider.web.By114;
import com.spider.web.Eporner;
import com.spider.web.Hanime;
import com.spider.web.Javfinder;
import com.spider.web.Javhihi;
import com.spider.web.Pornhub;
import com.spider.web.Wallhaven;
import com.spider.web.YoutubeSpider;

import ws.schild.jave.MultimediaInfo;
import ws.schild.jave.VideoInfo;

@SpringBootApplication
public class Application {

	static Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(Application.class);
		By114 By114=context.getBean(By114.class);
		By114.downloadBt();
	}
}
