package com.spider;

import com.alibaba.fastjson.JSON;
import com.spider.entity.AvInfo;
import com.spider.entity.Video;
import com.spider.service.AvInfoService;
import com.spider.service.VideoService;
import com.spider.web.Javbangers;
import com.spider.web.Javbus;
import com.spider.web.Pornhub;
import com.spider.web.Xslist;
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
//        AvInfoService avInfoService = context.getBean(AvInfoService.class);
//        List<AvInfo> avInfoList = avInfoService.findByRegex("code", "MKBD");
//        List<List<AvInfo.Magnet>> list = avInfoList.stream().filter(Objects::nonNull).filter(avInfo -> avInfo.getMagnetList().size() > 1).map(AvInfo::getMagnetList).collect(Collectors.toList());
//        List<AvInfo.Magnet> resultList=new ArrayList<>();
//        for(List<AvInfo.Magnet> magnetList:list){
//            //String line="";
//            for(AvInfo.Magnet magnet:magnetList){
//                if(magnet.getSize()<10240){
//                    //line=magnet.getMagnet();
//                    resultList.add(magnet);
//                    break;
//                }
//            }
//        }
//        for(AvInfo.Magnet magnet:resultList){
//            System.out.println(magnet.getMagnet());
//        }
//        System.out.println(list.size());
        Xslist Xslist=  context.getBean(Xslist.class);
        List<String> urlList= Xslist.getSearchList("有賀ゆあ");
        System.out.println(JSON.toJSONString(Xslist.getInfo(urlList.get(0))));
    }
}
