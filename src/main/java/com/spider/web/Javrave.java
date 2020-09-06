package com.spider.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spider.entity.Video;
import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class Javrave extends BaseWeb {

    @Value("${javrave.home}")
    private String home;

    @Value("${javrave.template}")
    private String template;

    @Value("${javrave.savePath}")
    private String savePath;

    @Value("${javrave.enableProxy}")
    private Boolean enableProxy;

    @Value("${javrave.thread}")
    private Integer thread;

    @Value("${javrave.api}")
    private String api;

    public List<Video> getVideoList(String category, Integer page) {
        List<Video> list = new ArrayList<>();
        String url = template.replace("@{category}", category).replace("@page", String.valueOf(page));
        Document document = JsoupUtil.getDocument(url, enableProxy);
        if (Objects.isNull(document)) {
            return list;
        }
        Elements elements = document.getElementsByTag("article");
        elements.stream().forEach(element -> {
            Elements h3 = element.getElementsByTag("h3");
            if (h3.size() > 0) {
                Elements as = h3.get(0).getElementsByTag("a");
                if (as.size() > 0) {
                    Video video = new Video();
                    String source = as.get(0).attr("href");
                    String name = as.get(0).text();
                    video.setSourceUrl(source);
                    video.setName(name);
                    list.add(video);
                }
            }
        });
        return list;
    }

    public Video getVideoInfo(String url) {
        Video video=new Video();
        Document document = JsoupUtil.getDocument(url, enableProxy);
        Elements elements = document.getElementsByTag("script");
        for (Element element : elements) {
            if (element.data().contains("iframecontent")) {
                String data = element.data();
                String iframecontent = data.split("var iframecontent=\"")[1].split("\";")[0];
                Document document1 = Jsoup.parse(iframecontent.replace("\\\"","\""));
                String src=  document1.getElementById("vid_iframe").attr("src");
                String key=src.split("/")[src.split("/").length-1];
                String json= OKHttpUtils.post(api.replace("@{key}",key),enableProxy);
                logger.info("{}",json);
                JSONObject jsonObject=JSON.parseObject(json);
                JSONObject fileData= jsonObject.getJSONArray("data").getJSONObject(jsonObject.getJSONArray("data").size()-1);
               // video.setName(fileData.get(""));
            }
        }
        return null;
    }
}
