package com.spider;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.fastjson.JSON;
import com.spider.entity.ActressesInfo;
import com.spider.entity.AvInfo;
import com.spider.entity.Video;
import com.spider.entity.es.EsAvInfo;
import com.spider.entity.es.EsVideo;
import com.spider.service.ActressesInfoService;
import com.spider.service.AvInfoService;
import com.spider.service.VideoService;
import com.spider.service.es.EsAvInfoService;
import com.spider.service.es.EsVideoService;
import com.spider.utils.FFmpegUtil;
import com.spider.web.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@SpringBootApplication
public class Application {

    static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class);
        Javbangers Javbangers=context.getBean(Javbangers.class);
        Javbangers.downloadUncensored();
    }
}
