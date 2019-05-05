package com.spider.web;

import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spider.utils.OKHttpUtils;
import com.spider.utils.download.ImageDownload;

@Service
public class Hanime {

	private Logger logger = LoggerFactory.getLogger(Hanime.class);

	@Value("${hanime.imageApi}")
	private String imageApi;

	@Autowired
	Proxy proxy;

	@Value("${hanime.thread}")
	private int thread;

	@Autowired
	ImageDownload imageDownload;

	@Value("${hanime.savePath}")
	private String savePath;

	public void downloadImage(String type) {
		int offset = 0;
		while (true) {
			try {
				String api = imageApi.replace("@{type}", type).replace("@{offset}", String.valueOf(offset * 24));
				Map<String, String> header = new HashMap<String, String>();
				header.put("x-directive", "api");
				String json = OKHttpUtils.get(api, header, proxy);
				logger.info(api);
				JSONObject jsonObject = JSON.parseObject(json);
				JSONArray array = jsonObject.getJSONArray("data");
				if (array.size() == 0) {
					break;
				}
				ExecutorService executorService = Executors.newFixedThreadPool(thread);
				for (int i = 0; i < array.size(); i++) {
					JSONObject data = array.getJSONObject(i);
					String filename = data.getString("filename");
					String url = data.getString("url");
					Integer width=data.getInteger("width");
					Integer height=data.getInteger("height");
					String extension=data.getString("extension");
					if(width*height>=1920*1080) {
						executorService.execute(new Runnable() {
							@Override
							public void run() {
								String name=UUID.randomUUID().toString().replace("-", "");
								String path = savePath + "\\" + type + "\\" + name+"."+extension;
								imageDownload.downloadFile(url, null, path, proxy);
								logger.info("{},{},下载完成", filename, url);
							}
						});
					}
					
				}
				executorService.shutdown();
				while (true) {
					if (executorService.isTerminated()) {
						logger.info("offset:{},下载完成!!!", String.valueOf(offset * 24));
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			offset++;
		}
	}

	public void Download_nsfw_general_Image() {
		downloadImage("nsfw_general");
	}

	public void Download_yuri_Image() {
		downloadImage("yuri");
	}

	public void Download_irl_3d_Image() {
		downloadImage("irl-3d");
	}

}
