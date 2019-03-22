package com.spider.web;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.spider.utils.OKHttpUtils;

@Service
public class Javfinder {

	@Value("${javfinder.home}")
	private String home;

	@Value("${javfinder.category.Uncensored}")
	private String Uncensored;

	@Autowired
	private Proxy proxy;

	public List<String> getVideoInfoUrlList(String url) {
		List<String> list = new ArrayList<String>();
		String html = OKHttpUtils.get(url, proxy);
		Document document = Jsoup.parse(html);
		Elements elements = document.getElementsByClass("main-thumb");
		for (Element element : elements) {
			String videoUrl = element.attr("href");
			list.add(videoUrl);
		}
		return list;
	}
	
	public Map<String,String> getVideoUrl(String url){
		return null;
	}
}
