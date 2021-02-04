package com.spider.web;

import com.spider.entity.Video;
import com.spider.utils.FFmpegUtil;
import com.spider.utils.JsoupUtil;
import com.spider.utils.MD5Util;
import com.spider.utils.download.HlsDownloader;
import io.lindstrom.m3u8.model.MasterPlaylist;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class Javhuge extends BaseWeb{

    @Value("${javhuge.home}")
    private String home;

    @Value("${javhuge.home}")
    private String template;

    @Value("${javhuge.savePath}")
    private String savePath;

    @Value("${javhuge.thread}")
    private Integer thread;

    @Value("${javhuge.enableProxy}")
    private boolean enableProxy;

    @Autowired
    private HlsDownloader hlsDownloader;

    public List<Map<String, String>> getVideoList(String category, Integer page) {
        String url = template.replace("@{category}", category).replace("@{page}", String.valueOf(page));
        Document document = JsoupUtil.getDocument(url,enableProxy);
        if (Objects.isNull(document)) {
            return null;
        }
        return document.getElementsByClass("entry-title").stream().map(element -> {
            if (element.getElementsByTag("a").size() != 0) {
                Map<String, String> map = new HashMap<>();
                String videoUrl = home + element.getElementsByTag("a").get(0).attr("href");
                String title = element.getElementsByTag("a").get(0).attr("title");
                map.put("url", videoUrl);
                map.put("title", title);
                return map;
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public String getVideoInfo(String url) {
        Document document = JsoupUtil.getDocument(url,enableProxy);
        if (Objects.isNull(document)) {
            return null;
        }
        Element script = document.getElementsByTag("script").stream().filter(element -> element.data().contains("uul")).collect(Collectors.toList()).get(0);
        return script.data().split("'")[1].replace("全集$", "").replace("在线播放$", "").replace("第01集$", "").replace("第1集$", "");
    }

    public void download(String category) {
        int page = 1;
        while (true) {
            try {
                List<Map<String, String>> list = getVideoList(category, page);
                if (CollectionUtils.isEmpty(list)) {
                    continue;
                }
                list.stream().sequential().forEach(map -> {
                    String url = map.get("url");
                    String title = map.get("title");
                    String masterM3u8 = getVideoInfo(url);
                    if(hasFilterKey(title)){
                        logger.info("{},包含过滤字段", title);
                        return;
                    }
                    String path = savePath + title.trim() + ".mp4";
                    Video video = new Video();
                    video.setName(title.trim());
                    video.setSavePath(path);
                    video.setSourceUrl(url);
                    MasterPlaylist masterPlaylist=hlsDownloader.getMasterPlaylist(masterM3u8,enableProxy);
                    if(Objects.nonNull(masterPlaylist)){
                        String m3u8=masterPlaylist.variants().get(0).uri();
                        if(!m3u8.startsWith("http")){
                            m3u8=masterM3u8.substring(0, masterM3u8.lastIndexOf("/") + 1)+m3u8;
                        }
                        video.setVideoUrl(m3u8);
                        hlsDownloader.downloadByVideo(video,thread,enableProxy);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            page++;
        }
    }


}
