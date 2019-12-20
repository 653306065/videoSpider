package com.spider.web;

import java.io.File;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spider.utils.JsoupUtil;
import com.spider.utils.download.MultithreadingDownload;

@Service
public class Pornhub {

	@Value("${pornhub.savePath}")
	private String savePath;

	@Value("${pornhub.home}")
	private String home;

	@Value("${pornhub.searchUrl}")
	private String searchUrl;
	
	@Autowired
	Proxy proxy;
	
	@Value("${pornhub.thread}")
	private int thread;
	
	@Autowired
	private MultithreadingDownload multithreadingDownload;

	public Map<String, String> getVideoByUrl(String url) {
		Document document = JsoupUtil.getDocumentByProxy(url);
		Element element = document.getElementById("player");
		String videoId = element.attr("data-video-id");
		String js = element.getElementsByTag("script").get(0).html().split("loadScriptUniqueId")[0];
		String json = getScriptJson(js, videoId);
		JSONObject jsonObject = JSON.parseObject(json);
		JSONObject mediaDefinitions = jsonObject.getJSONObject("mediaDefinitions");
		for (Entry<String, Object> entry : mediaDefinitions.entrySet()) {
			String videoUrl = mediaDefinitions.getJSONObject(entry.getKey()).getString("videoUrl");
			String quality = mediaDefinitions.getJSONObject(entry.getKey()).getString("quality");
			if (videoUrl != null && videoUrl.length() > 0) {
				Map<String, String> video = new HashMap<String, String>();
				String name = jsonObject.getString("video_title") + ".mp4";
				video.put("name", name.replace("|", ""));
				video.put("url", videoUrl);
				video.put("quality", quality);
				return video;
			}
		}
		return null;
	}

	public List<String> getVideoList(String url) {
		List<String> list = new ArrayList<String>();
		Document document = JsoupUtil.getDocumentByProxy(url);
		Elements elements = document.getElementById("videoSearchResult").getElementsByClass("title");
		for (Element element : elements) {
			String href = element.getElementsByTag("a").attr("href");
			if (href.indexOf("viewkey") != -1) {
				list.add(home + href);
			}
		}
		return list;
	}

	public void downloadSearch(String key) {
		int page = 1;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		while (true) {
			try {
				String url= searchUrl.replace("@{key}", key).replace("@{page}", String.valueOf(page));
				System.out.println(url);
				List<String> list= getVideoList(url);
				for(String str:list) {
					Map<String,String> map= getVideoByUrl(str);
					String httpUrl=map.get("url");
					String name=map.get("name");
					String quality=map.get("quality");
					String date = simpleDateFormat.format(new Date());
					String path=savePath+key+File.separator+date+File.separator+name;
					if(Integer.valueOf(quality)<720) {
						continue;
					}
					multithreadingDownload.fileDownload(httpUrl, path, null, proxy, thread);
				}
				if(list.size()==0) {
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			page++;
		}
	}
	
	public void downloadHmv() {
		downloadSearch("hmv");
	}
	
	public void downloadMMD() {
		downloadSearch("mmd");
	}
	
	public void downloadsfm() {
		downloadSearch("sfm");
	}

	public String getScriptJson(String js, String videoId) {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("javascript");
		try {
			engine.eval(js);
			return JSON.toJSONString(engine.get("flashvars_" + videoId));
		} catch (ScriptException e) {
			e.printStackTrace();
			return null;
		}
	}
}