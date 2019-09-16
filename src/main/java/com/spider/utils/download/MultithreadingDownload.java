package com.spider.utils.download;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.spider.utils.ConsoleProgressBar;
import com.spider.utils.OKHttpUtils;
import okhttp3.Response;

@Component
public class MultithreadingDownload {

	public static volatile long downloadByte = 0;
	
	private Logger logger =LoggerFactory.getLogger(MultithreadingDownload.class);

	public void fileDownload(String HttpUrl,String path, Map<String, String> header,
			Proxy proxy, int threadNum) {
		try {
			long startTime = System.currentTimeMillis();
			File file = new File(path);
			if (file.exists()) {
				logger.info(path + ",已存在");
				return;
			} else {
				file.getParentFile().mkdirs();
			}
			DownloadFileInfo info = getDownloadFileInfo(HttpUrl,header, proxy);
			if (!String.valueOf(info.getResponseCode()).startsWith("20")) {
				logger.info("----获取下载信息错误：responseCode=" + info.getResponseCode() + "----");
				return;
			} else {
				logger.info(path+",开始下载,url:" + HttpUrl);
				logger.info(JSON.toJSONString(info) + ",大小" + (info.getContentLength() / 1024.0 / 1024.0) + "m");
				long size = info.getContentLength() / threadNum;
				RandomAccessFile raf = new RandomAccessFile(file, "rw");
				raf.setLength(info.getContentLength());
				ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
				for (int i = 0; i < threadNum; i++) {
					long startByte = i * size;
					long endByte = (i + 1) * size - 1;
					if (i == threadNum) {
						endByte = info.getContentLength();
					}
					DownloadThread thread = new DownloadThread(HttpUrl, header, proxy, startByte,
							endByte, file);
					executorService.execute(thread);
				}
				executorService.shutdown();
				ConsoleProgressBar cpb = new ConsoleProgressBar(100, '#');
				while (true) {
					if (executorService.isTerminated()) {
						break;
					}
					double percentage = (MultithreadingDownload.downloadByte * 1.0) / (info.getContentLength() * 1.0) * 100.0;
					cpb.show((int) Math.floor(percentage));
					//logger.info("----已下载:" + String.valueOf(percentage) + "%-----");
					Thread.sleep(1000 * 2);
				}
				downloadByte = 0;
				logger.info("----" + path + ",下载完成----");
				long endTime = System.currentTimeMillis();
				logger.info("耗时:" + (endTime - startTime) / 1000 / 60.0 + "分钟");
				raf.close();
			}
		} catch (Exception e) {
			downloadByte = 0;
			logger.info("----下载异常----");
			new File(path).delete();
			e.printStackTrace();
		}

	}

	private DownloadFileInfo getDownloadFileInfo(String HttpUrl, Map<String, String> header, Proxy proxy) {
		try {
			Response response= OKHttpUtils.getResponse(HttpUrl, header, proxy);
			DownloadFileInfo fileInfo=new DownloadFileInfo();
			if(response.header("Content-Length")!=null) {
				fileInfo.setContentLength(Long.valueOf(response.header("Content-Length")));
			}
			fileInfo.setContentType(response.header("Content-Type"));
			fileInfo.setResponseCode(response.code());
			fileInfo.setEtag(response.header("etag"));
			return fileInfo;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
