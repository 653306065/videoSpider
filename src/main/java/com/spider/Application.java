package com.spider;

import com.alibaba.fastjson.JSON;
import com.spider.entity.AvInfo;
import com.spider.entity.Video;
import com.spider.service.AvInfoService;
import com.spider.service.VideoService;
import com.spider.utils.FFmpegUtil;
import com.spider.utils.FileUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Date;

@SpringBootApplication
public class Application {

    static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class);
        AvInfoService avInfoService = context.getBean(AvInfoService.class);
        VideoService videoService = context.getBean(VideoService.class);
//        List<AvInfo> list = avInfoService.findAll();
//        list.stream().parallel().forEach(avInfo -> {
//             Video find= videoService.findOneByRegex("name",avInfo.getCode().toLowerCase());
//             Video find1= videoService.findOneByRegex("name",avInfo.getCode().toUpperCase());
//             if(Objects.nonNull(find)){
//                 System.out.println(find.getName());
//             }
//            if(Objects.nonNull(find1)){
//                System.out.println(find1.getName());
//            }
//        });
        List<File> list = FileUtils.getPathFileList("D:\\javfinder", new ArrayList<>());
        list.stream().parallel().forEach(file -> {
            if(videoService.findByName(file.getName())==null){
                Video video = new Video();
                video.setName(file.getName());
                video.setMd5(FileUtils.getMD5(file));
                video.setSavePath(file.getAbsolutePath());
                video.setSize(file.length());
                video.setMultimediaInfo(FFmpegUtil.getVideoInfo(file));
                video.setCreateDate(new Date());
                video.setFormat("mp4");
                videoService.insert(video);
                logger.info("{},保存完成",video.getName());
            }
        });
    }
}
