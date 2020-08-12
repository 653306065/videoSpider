package com.spider.aop;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSON;
import org.apache.http.protocol.HTTP;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.spider.entity.Image;
import com.spider.entity.Video;
import com.spider.service.ImageService;
import com.spider.service.UrlRecordService;
import com.spider.service.VideoService;
import com.spider.utils.FFmpegUtil;
import com.spider.utils.FileUtils;
import com.spider.utils.ImageUtils;

import ws.schild.jave.MultimediaInfo;

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

    @Pointcut("execution(* com.spider.utils.download.MultithreadingDownload.videoDownload(..))")
    public void multithreadingDownload_videoDownload() {

    }

    @Around(value="multithreadingDownload_videoDownload()")
    public void aroundVideoDownload(ProceedingJoinPoint joinPoint){
        Object[] args = joinPoint.getArgs();
        Video video= (Video) args[0];

        if(Objects.nonNull(videoService.findByName(video.getName()))){
            logger.info(video.getName() + "已存在");
            return;
        }
        if(Objects.nonNull(video.getMd5())&&Objects.nonNull(videoService.findByMd5(video.getMd5()))){
            logger.info(video.getName() + "已存在");
            return;
        }

        if(Objects.nonNull(video.getVideoUrl())&&Objects.nonNull(videoService.findByVideoUrl(video.getVideoUrl()))){
            logger.info(video.getName() + "已存在");
            return;
        }

        if(Objects.nonNull(video.getSourceUrl())&&Objects.nonNull(videoService.findBySourceUrl(video.getSourceUrl()))){
            logger.info(video.getName() + "已存在");
            return;
        }

        try {
            logger.info("{},文件名验证通过", video.getName());
            File videoFile=new File(video.getSavePath());
            Boolean result = (Boolean) joinPoint.proceed();
            if(!result){
                logger.info("{},文件下载失败",video.getName());
                return;
            }
            String md5 = FileUtils.getMD5(videoFile);
            if(Objects.isNull(videoService.findByMd5(md5))){
                video.setMd5(md5);
                MultimediaInfo info = FFmpegUtil.getVideoInfo(videoFile);
                logger.info("videoInfo:{}",JSON.toJSONString(info));
                video.setSize(videoFile.length());
                video.setSizeStr(videoFile.length() / 1024.0 / 1024 + "MB");
                video.setMultimediaInfo(info);
                video.setCreateDate(new Date());
                videoService.insert(video);
                logger.info("{},文件信息保存完成",video.getName());
            }else{
                new File(video.getSavePath()).delete();
            }
        }catch (Throwable e){
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
