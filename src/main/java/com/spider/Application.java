package com.spider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.spider.utils.FileUtils;
import com.spider.utils.ImageUtils;
import com.spider.web.Eporner;
import com.spider.web.Hanime;
import com.spider.web.Longteng;
import com.spider.web.Wallhaven;
import com.spider.web.Javfinder;

@SpringBootApplication
public class Application {

	static Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(Application.class);
		Javfinder javfinder=context.getBean(Javfinder.class);
		javfinder.downloadUncensored();
	}
}
