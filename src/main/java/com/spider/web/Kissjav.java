package com.spider.web;

import com.spider.constant.Constant;
import com.spider.entity.Video;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;
import com.spider.utils.download.MultithreadingDownload;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class Kissjav extends BaseWeb {

    @Value("${kissjav.template}")
    private String template;

    @Value("${kissjav.cookie}")
    private String cookie;

    public List<Video> getVideoList(String category, Integer page) {
        List<Video> list = new ArrayList<>();
        String url = template.replace("@{category}", category).replace("@{page}", String.valueOf(page));
        logger.info("-------------{}--------------", url);
        Document document = JsoupUtil.getDocument(url, enableProxy);
        if (document == null) {
            return list;
        }
        Elements elements = document.getElementsByClass("video");
        elements.forEach(element -> {
            Video video = new Video();
            if (element.getElementsByTag("a").size() != 0) {
                Element a = element.getElementsByTag("a").get(0);
                String name = a.attr("title");
                String source = home + a.attr("href");
                video.setName(name);
                video.setSourceUrl(source);
                list.add(video);
            }
        });
        return list;
    }

    public Video getVideoInfo(String url) {
        Video video = new Video();
        video.setSourceUrl(url);
        Document document = JsoupUtil.getDocument(url, enableProxy);
        if (Objects.isNull(document)) {
            return null;
        }
        Elements h1s = document.getElementsByTag("h1");
        if (Objects.nonNull(h1s) && h1s.size() != 0) {
            video.setName(h1s.get(0).text());
        }
        Elements elements = document.getElementsByTag("source");
        if (Objects.nonNull(elements) && elements.size() != 0) {
            String redirection = elements.get(0).attr("src");
            Map<String, String> map = new HashMap<>();
            String[] valueArr = url.split("/");
            map.put("Referer", url.replace(valueArr[4], URLEncoder.encode(valueArr[4])));
            map.put("Cookie", cookie);
            map.put("User-Agent", Constant.user_agent);
            map.put("Accept", "*/*");
            map.put("Accept-Encoding", "gzip, deflate, br");
            map.put("Connection", "keep-alive");
            map.put("Host", "kissjav.com");
            String realUrl = OKHttpUtils.getRedirectUrl(redirection, map, enableProxy);
            video.setVideoUrl(realUrl);
        }
        return video;
    }

    public void downloadVideo(String category) {
        int page = 2;
        while (true) {
            try {
                List<Video> videoList = getVideoList(category, page);
                for (Video video : videoList) {
                    try {
                        logger.info("{},开始下载", video.getName());
                        if (videoExistVerify(video)) {
                            Video getVideo = getVideoInfo(video.getSourceUrl());
                            if (Objects.nonNull(getVideo) && Objects.nonNull(getVideo.getVideoUrl()) && videoExistVerify(getVideo)) {
                                getVideo.setName(FileUtils.repairPath(getVideo.getName()) + ".mp4");
                                String path = savePath + category + fileSeparator + simpleDateFormat.format(new Date()) + fileSeparator + getVideo.getName();
                                getVideo.setSavePath(path);
                                multithreadingDownload.videoDownload(getVideo, null, enableProxy, thread, defaultSegmentSize);
                                logger.info("{},下载完成", video.getName());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            page++;
        }
    }

    public void downloadJavUncensored() {
        downloadVideo("jav-uncensored");
    }
}
