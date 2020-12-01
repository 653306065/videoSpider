package com.spider.web;

import com.spider.utils.FFmpegUtil;
import com.spider.utils.JsoupUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class Javhuge {

    static Logger logger = LoggerFactory.getLogger(Javhuge.class);

    @Value("${javhuge.home}")
    private String home;

    @Value("${javhuge.home}")
    private String template;

    @Value("${javhuge.savePath}")
    private String savePath;

    @Value("${filterKey}")
    private List<String> filterKey;


    public List<Map<String, String>> getVideoList(String category, Integer page) {
        String url = template.replace("@{category}", category).replace("@{page}", String.valueOf(page));
        Document document = JsoupUtil.getDocument(url);
        if (Objects.isNull(document)) {
            return null;
        }
        return document.getElementsByClass("entry-title").stream().map(element -> {
            if (element.getElementsByTag("a").size() != 0) {
                Map<String, String> map = new HashMap<>();
                String videoUrl = home + element.getElementsByTag("a").get(0).attr("href");
                String title =element.getElementsByTag("a").get(0).attr("title");
                map.put("url", videoUrl);
                map.put("title", title);
                return map;
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public String getVideoInfo(String url) {
        Document document = JsoupUtil.getDocument(url);
        if (Objects.isNull(document)) {
            return null;
        }
        Element script = document.getElementsByTag("script").stream().filter(element -> element.data().contains("uul")).collect(Collectors.toList()).get(0);
        return script.data().split("'")[1].replace("全集$", "").replace("在线播放$", "").replace("第01集$", "").replace("第1集$", "");
    }

    public void download(String category) {
        int page=1;
        while (true){
            List<Map<String, String>> list = getVideoList(category, page);
            list.stream().parallel().forEach(map -> {
                String url = map.get("url");
                String title = map.get("title");
                String m3u8 = getVideoInfo(url);
                for(String key:filterKey){
                  if(title.contains(key)){
                      logger.info("{},包含过滤字段:{}",title,key);
                      return;
                  }
                }
                FFmpegUtil.downloadM3U8(m3u8, savePath + title.trim() + ".mp4");
            });
            page++;
        }
    }


}
