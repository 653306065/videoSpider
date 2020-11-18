package com.spider.aop;

import com.alibaba.fastjson.JSON;
import com.spider.entity.AvInfo;
import com.spider.entity.Image;
import com.spider.entity.Video;
import com.spider.service.*;
import com.spider.utils.FFmpegUtil;
import com.spider.utils.FileUtils;
import com.spider.utils.ImageUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ws.schild.jave.MultimediaInfo;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Aspect
public class DownloadAop {

    Logger logger = LoggerFactory.getLogger(DownloadAop.class);

    @Autowired
    private VideoService videoService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UrlRecordService urlRecordService;

    @Autowired
    private AvInfoService avInfoService;

    @Pointcut("execution(* com.spider.utils.download.MultithreadingDownload.videoDownload(..))")
    public void multithreadingDownload_videoDownload() {

    }

    @Around(value = "multithreadingDownload_videoDownload()")
    public void aroundVideoDownload(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Video video = (Video) args[0];
        String name = video.getName();
        List<String> list = FileUtils.getSearchKeyList(name);
        AvInfo avInfo = null;
        //根据名称AV code判断视频是否存在
        for (String key : list) {
            avInfo = avInfoService.findOnekeyValue("code", key);
            if (Objects.nonNull(avInfo)) {
                logger.info("{},的code是{}", video.getName(), avInfo.getCode());
                if (avInfo.isHasVideo()) {
                    logger.info("code,{},的视频已存在,vidoeId:{},savePath:{}", avInfo.getCode(), avInfo.getVideoId(), avInfo.getVideoSavePath());
                    return;
                }
                break;
            }
        }

        //确认视频是否为av
        for (Map.Entry<String, List<String>> entry : AvInfoService.codeTransformMap.entrySet()) {
            for (String code : entry.getValue()) {
                if (name.contains(code)) {
                    video.setAvCode(entry.getKey());
                }
            }
        }

        //根据avcode判断视频是否存在
        if (Objects.nonNull(video.getAvCode())) {
            avInfo = avInfoService.findOnekeyValue("code", video.getAvCode());
            Video findVideo = videoService.findOnekeyValue("avCode", video.getAvCode());
            if (Objects.nonNull(avInfo) && avInfo.isHasVideo()) {
                logger.info("code,{},的视频已存在,vidoeId:{},savePath:{}", avInfo.getCode(), avInfo.getVideoId(), avInfo.getVideoSavePath());
                return;
            }
            if (Objects.nonNull(findVideo)) {
                logger.info("code,{},的视频已存在,vidoeId:{},savePath:{}", findVideo.getAvCode(), findVideo.getId(), findVideo.getSavePath());
                return;
            }
        }

//        if(avInfo==null){
//            logger.info("{},没有匹配到code",video.getName());
//            return;
//        }
        if (Objects.nonNull(videoService.findByName(video.getName()))) {
            logger.info(video.getName() + "已存在");
            return;
        }
        if (Objects.nonNull(video.getMd5()) && Objects.nonNull(videoService.findByMd5(video.getMd5()))) {
            logger.info(video.getName() + "已存在");
            return;
        }

        if (Objects.nonNull(video.getVideoUrl()) && Objects.nonNull(videoService.findByVideoUrl(video.getVideoUrl()))) {
            logger.info(video.getName() + "已存在");
            return;
        }

        if (Objects.nonNull(video.getSourceUrl()) && Objects.nonNull(videoService.findBySourceUrl(video.getSourceUrl()))) {
            logger.info(video.getName() + "已存在");
            return;
        }

        try {
            logger.info("{},验证通过", video.getName());
            File videoFile = new File(video.getSavePath());
            Boolean result = (Boolean) joinPoint.proceed();
            if (!result) {
                logger.info("{},文件下载失败", video.getName());
                return;
            }
            String md5 = FileUtils.getMD5(videoFile);
            if (Objects.isNull(videoService.findByMd5(md5))) {
                video.setMd5(md5);
                MultimediaInfo info = FFmpegUtil.getVideoInfo(videoFile);
                logger.info("videoInfo:{}", JSON.toJSONString(info));
                video.setSize(videoFile.length());
                video.setSizeStr(videoFile.length() / 1024.0 / 1024 + "MB");
                video.setMultimediaInfo(info);
                video.setCreateDate(new Date());
                if (Objects.nonNull(avInfo)) {
                    video.setAvCode(avInfo.getCode());
                }
                videoService.insert(video);
                if (Objects.nonNull(avInfo)) {
                    avInfo.setVideoId(video.getId());
                    avInfo.setHasVideo(true);
                    avInfo.setVideoSavePath(video.getSavePath());
                    avInfoService.updateById(avInfo);
                }

                if (Objects.nonNull(video.getAvCode())) {
                    avInfo = avInfoService.findOnekeyValue("code", video.getAvCode());
                    if (Objects.nonNull(avInfo)) {
                        avInfo.setVideoId(video.getId());
                        avInfo.setHasVideo(true);
                        avInfo.setVideoSavePath(video.getSavePath());
                        avInfoService.updateById(avInfo);
                    }
                }
                logger.info("{},文件信息保存完成", video.getName());
            } else {
                logger.info("{},文件MD5验证失败", video.getName());
                new File(video.getSavePath()).delete();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Pointcut("execution(* com.spider.utils.download.ImageDownload.downloadFile(..))")
    public void imageDownload_downloadFile() {

    }

    @Around(value = "imageDownload_downloadFile()")
    public void aroundImageDownload_downloadFile(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String httpUrl = String.valueOf(args[0]);
        String path = String.valueOf(args[2]);
        File file = new File(path);
        if (urlRecordService.existUrl(httpUrl)) {
            logger.info(httpUrl + ",已存在");
            return;
        }
        try {
            if (imageService.findByUrl(httpUrl) != null) {
                logger.info("{},url已存在", httpUrl);
                return;
            }
            joinPoint.proceed();
            urlRecordService.insert(httpUrl);
            File imageFile = new File(path);
            Image newImage = new Image();
            if (!imageFile.exists()) {
                logger.info("{}文件不存在", imageFile.getAbsoluteFile());
                return;
            }
            String md5 = FileUtils.getMD5(imageFile);
            if (imageService.findByMd5(md5) == null) {
                logger.info("{},md5验证通过", file.getName());
                newImage.setMd5(md5);
                newImage.setName(imageFile.getName());
                newImage.setSavePath(imageFile.getAbsolutePath());
                newImage.setSize(imageFile.length());
                newImage.setSizeStr(imageFile.length() / 1024.0 + "KB");
                newImage.setSource(httpUrl);
                newImage.setCreateDate(new Date());
                int height = 1080;
                int width = 1920;
                try {
                    height = ImageUtils.getImgHeight(imageFile);
                    width = ImageUtils.getImgWidth(imageFile);
                    newImage.setHeight(height);
                    newImage.setWidth(width);
                    if (height * width < 1280 * 720 && (height != -1 || width != -1)) {
                        imageFile.delete();
                        logger.info("{}分辨率太低,删除成功", file.getName());

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                imageService.insert(newImage);
                logger.info("{},文件信息存储完成", newImage.getName());
            } else {
                logger.info("{},md5验证失败", file.getName());
                if (imageFile.exists() && !imageFile.getAbsolutePath().equals(imageService.findByMd5(md5).getSavePath())) {
                    boolean b = imageFile.delete();
                    if (b) {
                        logger.info("{},删除成功", file.getName());
                    } else {
                        logger.info("{},删除失败", file.getName());
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
