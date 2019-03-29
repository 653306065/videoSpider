package com.spider.aop;

import java.io.File;
import java.util.Date;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.spider.entity.Image;
import com.spider.entity.Video;
import com.spider.service.ImageService;
import com.spider.service.VideoService;
import com.spider.utils.FileUtils;
import com.spider.utils.ImageUtils;

@Component
@Aspect
public class DownloadAop {

	Logger logger = LoggerFactory.getLogger(DownloadAop.class);

	@Autowired
	private VideoService videoService;
	
	@Autowired
	private ImageService imageService;

	@Pointcut("execution(* com.spider.utils.download.MultithreadingDownload.fileDownload(..))")
	public void multithreadingDownload_fileDownload() {

	}

	@Around(value = "multithreadingDownload_fileDownload()")
	public void aroundMultithreadingDownload(ProceedingJoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		String httpUrl=String.valueOf(args[0]);
		String path = String.valueOf(args[1]);
		File file = new File(path);
		Video video = videoService.findByName(file.getName());
		if (video != null) {
			logger.info(file.getName() + "已存在");
		} else {
			try {
				logger.info("{},文件名验证通过", file.getName());
				joinPoint.proceed();
				File videoFile=new File(path);
				Video newvideo = new Video();
				String md5=FileUtils.getMD5(videoFile);
				if(videoService.findByMd5(md5)==null){
					logger.info("{},md5验证通过", file.getName());
					newvideo.setMd5(md5);
					newvideo.setName(videoFile.getName());
					newvideo.setSavePath(videoFile.getAbsolutePath());
					newvideo.setSize(videoFile.length());
					newvideo.setSizeStr(videoFile.length()/1024.0/1024+"MB");
					newvideo.setSource(httpUrl);
					newvideo.setCreateDate(new Date());
					videoService.insert(newvideo);
					logger.info("{},文件信息存储完成",newvideo.getName());
				}else {
					logger.info("{},md5验证失败", file.getName());
					videoFile.delete();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	
	@Pointcut("execution(* com.spider.utils.download.ImageDownload.downloadFile(..))")
	public void imageDownload_downloadFile() {

	}
	
	@Around(value = "imageDownload_downloadFile()")
	public void aroundImageDownload_downloadFile(ProceedingJoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		String httpUrl=String.valueOf(args[0]);
		String path = String.valueOf(args[2]);
		File file = new File(path);
		Image image = imageService.findByName(file.getName());
		if (image != null) {
			logger.info(file.getName() + "已存在");
		} else {
			try {
				logger.info("{},文件名验证通过", file.getName());
				joinPoint.proceed();
				File imageFile=new File(path);
				Image newImage = new Image();
				String md5=FileUtils.getMD5(imageFile);
				if(imageService.findByMd5(md5)==null){
					logger.info("{},md5验证通过", file.getName());
					newImage.setMd5(md5);
					newImage.setName(imageFile.getName());
					newImage.setSavePath(imageFile.getAbsolutePath());
					newImage.setSize(imageFile.length());
					newImage.setSizeStr(imageFile.length()/1024.0/1024+"MB");
					newImage.setSource(httpUrl);
					newImage.setCreateDate(new Date());
					int height = ImageUtils.getImgHeight(imageFile);
					int width = ImageUtils.getImgWidth(imageFile);
					newImage.setHeight(height);
					newImage.setWidth(width);
					imageService.insert(newImage);
					logger.info("{},文件信息存储完成",newImage.getName());
				}else {
					logger.info("{},md5验证失败", file.getName());
					imageFile.delete();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}
