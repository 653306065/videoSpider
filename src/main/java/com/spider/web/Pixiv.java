package com.spider.web;

import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
public class Pixiv {

	private Logger logger = LoggerFactory.getLogger(Pixiv.class);

	@Value("${pixiv.todayRank.R-18}")
	private String R18Url;

	@Value("${pixiv.cookie}")
	private String cookie;

	@Value("${pixiv.home}")
	private String home;

	@Autowired
	Proxy proxy;
	
	@Value("${pixiv.savePath}")
	private String savePath;
	
	@Autowired
	ImageDownload imageDownload;

	public List<String> getHistoryRankListUrl(Date date) {
		List<String> urlList = new ArrayList<String>();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
		String dateStr = simpleDateFormat.format(date);
		String url = R18Url.replace("@{date}", dateStr).replace("@{page}", String.valueOf(1));
		Map<String, String> header = new HashMap<>();
		header.put("cookie", cookie);
		String json = OKHttpUtils.get(url, header, proxy);
		JSONObject jsonObject = JSON.parseObject(json);
		JSONArray jsonArray = jsonObject.getJSONArray("contents");
		for (int i = 0; i < jsonArray.size(); i++) {
			logger.info(jsonArray.getJSONObject(i).getString("url"));
			String imageurl = jsonArray.getJSONObject(i).getString("url").replace("c/240x480/img-master", "img-original").replace("_master1200", "");
			logger.info(imageurl);
			urlList.add(imageurl);
		}
		System.out.println(json);
		return urlList;
	}
	
	public void downloadHistoryR18(){
		Date date=new Date(System.currentTimeMillis()-1000*60*60*24*356);
		while(true) {
			List<String> urls=getHistoryRankListUrl(date);
			for(String url:urls) {
				Map<String, String> header = new HashMap<>();
				header.put("Referer", "https://www.pixiv.net/ranking.php?mode=daily&content=illust");
				imageDownload.downloadFile(url,header,savePath+"\\"+UUID.randomUUID()+".jpg", proxy);
			}
			date.setTime(date.getTime()+1000*60*60*24);
		}
	}
}
