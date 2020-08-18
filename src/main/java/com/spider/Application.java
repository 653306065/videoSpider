package com.spider;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.spider.entity.Video;
import com.spider.service.VideoService;
import com.spider.web.*;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import ws.schild.jave.MultimediaInfo;
import ws.schild.jave.VideoInfo;

@SpringBootApplication
public class Application {

    static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class);
        Javbus Javbus=context.getBean(Javbus.class);
        Javbus.saveAllUncensoredActressesInfo();
    }
}
