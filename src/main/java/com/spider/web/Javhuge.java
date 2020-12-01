package com.spider.web;

import com.spider.entity.Video;
import com.spider.service.AvInfoService;
import com.spider.service.VideoService;
import com.spider.utils.FFmpegUtil;
import com.spider.utils.JsoupUtil;
import com.spider.utils.MD5Util;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
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

    @Autowired
    VideoService videoService;

    @Autowired
    AvInfoService avInfoService;


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
        Document document = JsoupUtil.getDocument(url);
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
                if(CollectionUtils.isEmpty(list)){
                    continue;
                }
                list.stream().parallel().forEach(map -> {
                    String url = map.get("url");
                    String title = map.get("title");
                    String m3u8 = getVideoInfo(url);
                    for (String key : filterKey) {
                        if (title.contains(key)) {
                            logger.info("{},包含过滤字段:{}", title, key);
                            return;
                        }
                    }
                    AtomicReference<String> avCode = new AtomicReference<>(null);
                    AvInfoService.codeTransformMap.entrySet().stream().forEach(entry -> {
                        entry.getValue().stream().forEach(code -> {
                            if (title.contains(code)) {
                                logger.info("{},的code为{}", title, entry.getKey());
                                avCode.set(entry.getKey());
                            }
                        });
                    });

                    if (Objects.nonNull(avCode.get())) {
                        Video video = videoService.findOnekeyValue("avCode", avCode.get());
                        if (Objects.nonNull(video)) {
                            logger.info("{},的视频已存在,savePath:{}", title, video.getSavePath());
                            return;
                        }
                    }
                    String path = savePath + title.trim() + ".mp4";
                    FFmpegUtil.downloadM3U8(m3u8, path);
                    Video video = new Video();
                    video.setName(title.trim());
                    video.setSavePath(path);
                    video.setSourceUrl(m3u8);
                    video.setVideoUrl(m3u8);
                    video.setAvCode(avCode.get());
                    video.setFormat("mp4");
                    video.setMd5(MD5Util.md5(new File(path)));
                    videoService.insert(video);
                    logger.info("{},保存完成", title);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            page++;
        }
    }


}
