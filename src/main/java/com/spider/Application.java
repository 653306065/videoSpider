package com.spider;

import com.alibaba.fastjson.JSON;
import com.spider.entity.AvInfo;
import com.spider.entity.Video;
import com.spider.service.AvInfoService;
import com.spider.service.VideoService;
import com.spider.utils.FFmpegUtil;
import com.spider.utils.FileUtils;
import com.spider.web.Javbangers;
import com.spider.web.Javbus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import com.spider.web.Javhihi;

@SpringBootApplication
public class Application {

    static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class);
        Javbus Javbus=context.getBean(Javbus.class);
        Javbus.updateAVMagnetList();
    }
}
