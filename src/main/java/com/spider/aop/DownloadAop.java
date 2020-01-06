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

	@Pointcut("execution(* com.spider.utils.download.MultithreadingDownload.fileDownload(..))")
	public void multithreadingDownload_fileDownload() {

	}

	@Around(value = "multithreadingDownload_fileDownload()")
	public void aroundMultithreadingDownload(ProceedingJoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		String httpUrl = String.valueOf(args[0]);
		String path = String.valueOf(args[1]);
		File file = new File(path);
		if(urlRecordService.existUrl(httpUrl)) {
			logger.info(httpUrl + ",已存在");
			return;
		} 
		Video video = videoService.findByName(file.getName());
		if (video != null) {
			logger.info(file.getName() + "已存在");
			return;
		} else {
			try {
				logger.info("{},文件名验证通过", file.getName());
				joinPoint.proceed();
				urlRecordService.insert(httpUrl);
				File videoFile = new File(path);
				Video newvideo = new Video();
				String md5 = FileUtils.getMD5(videoFile);
				if (videoService.findByMd5(md5) == null) {
					logger.info("{},md5验证通过", file.getName());
					newvideo.setMd5(md5);
					newvideo.setName(videoFile.getName());
					newvideo.setSavePath(videoFile.getAbsolutePath());
					newvideo.setSize(videoFile.length());
					newvideo.setSizeStr(videoFile.length() / 1024.0 / 1024 + "MB");
					newvideo.setSource(httpUrl);
					newvideo.setCreateDate(new Date());
					videoService.insert(newvideo);
					MultimediaInfo info= FFmpegUtil.getVideoInfo(videoFile);
					if(info!=null) {
						long duration = info.getDuration();
						int height = info.getVideo().getSize().getHeight();
						int width = info.getVideo().getSize().getWidth();
						if (height*width<1280*720) {
							boolean b= videoFile.delete();
							if(b) {
								logger.info("{}分辨率太低,删除成功",file.getName());
							}else {
								logger.info("{}分辨率太低,删除失败",file.getName());
							}
						}
					}
					logger.info("{},文件信息存储完成", newvideo.getName());
				} else {
					logger.info("{},md5验证失败", file.getName());
					if (videoFile.exists()) {
						boolean b= videoFile.delete();
						if(b) {
							logger.info("{},删除成功",file.getName());
						}else {
							logger.info("{},删除失败",file.getName());
						}
					}
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
		String httpUrl = String.valueOf(args[0]);
		String path = String.valueOf(args[2]);
		File file = new File(path);
		if(urlRecordService.existUrl(httpUrl)) {
			logger.info(httpUrl + ",已存在");
			return;
		}
		try {
			if(imageService.findByUrl(httpUrl)!=null) {
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
				} catch (Exception e) {
					e.printStackTrace();
				}
				imageService.insert(newImage);
				logger.info("{},文件信息存储完成", newImage.getName());
			} else {
				logger.info("{},md5验证失败", file.getName());
				if (imageFile.exists()&&!imageFile.getAbsolutePath().equals(imageService.findByMd5(md5).getSavePath())) {
					boolean b=imageFile.delete();
					if(b) {
						logger.info("{},删除成功",file.getName());
					}else {
						logger.info("{},删除失败",file.getName());
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
