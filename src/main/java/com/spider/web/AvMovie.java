package com.spider.web;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.spider.utils.JsoupUtil;

@Service
public class AvMovie {

    @Value("${avMovie.home}")
    private String home;

    @Value("${avMovie.savePath}")
    private String savePath;

    @Value("${avMovie.uncensored}")
    private String uncensored;

    @Value("${avMovie.videoUrl}")
    private String videoUrl;

    @Value("${avMovie.videoListUrl}")
    private String videoListUrl;

    public List<String> getVideoUrl(String type, int page) {
        List<String> list = new ArrayList<String>();
        String realUrl = videoListUrl.replace("@{page}", String.valueOf(page)).replace("@{type}", type);
        Document document = JsoupUtil.getDocumentByProxy(realUrl);
        Elements elements = document.getElementsByClass("video-item-wrapper");
        for (Element element : elements) {
            String href = "http:" + element.getElementsByTag("a").get(0).attr("href");
            list.add(href);
        }
        return list;
    }

    public String getVideoId(String url) {
        Document document = JsoupUtil.getDocumentByProxy(url);
        String videoId = document.toString().split("videoId")[1].split(";")[0].replaceAll("\"", "").replaceAll("=", "").trim();
        System.out.println(videoId);
        return url;
    }

//	public String getIframeUrl(String videoId) {
//		
//	}
}
