package com.spider.web;

import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;

@Component
public class Youtube {

	@Value("${youtube.savePath}")
	private String savePath;
	
	@Value("${youtube.home}")
	private String home;
	
	@Value("${youtube.thread}")
	private Integer thread;
	
	@Value("${youtube.api}")
	private String api;
	
	@Autowired
	private Proxy proxy;
	
	public String getApiToken() {
		Document document= JsoupUtil.getDocumentByProxy(home);
		String csrf_token= document.getElementById("csrf_token").attr("value");
		return csrf_token;
	}
	
	public String getVideoUrlList(String url) {
		Map<String,String> params=new HashMap<String, String>();
		params.put("url", url);
		params.put("csrf_token", getApiToken());
		params.put("proxy", "Random");
		String html= OKHttpUtils.postFormData(url, params, proxy);
		Document document=Jsoup.parse(html);
		return html;
	}
}
