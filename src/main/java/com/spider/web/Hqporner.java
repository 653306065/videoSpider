package com.spider.web;

import com.spider.entity.Video;
import com.spider.utils.JsoupUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class Hqporner extends BaseWeb {

    @Value("${hqporner.template}")
    private String template;

    public List<String> getVideoList(String category, Integer page) {
        String url = template.replace("@{category}", category).replace("@{page}", String.valueOf(page));
        Document document = JsoupUtil.getDocument(url, enableProxy);
        if (Objects.isNull(document)) {
            return null;
        }
        return document.getElementsByClass("meta-data-title").stream().map(element -> home + element.getElementsByTag("a").get(0).attr("href")).collect(Collectors.toList());
    }

    public Video getVideo(String url) {
        Document document = JsoupUtil.getDocument(url, enableProxy);
        if (Objects.isNull(document)) {
            return null;
        }
        String name = document.getElementsByClass("main-h1").get(0).text();
        String startName = document.getElementsByClass("icon fa-star-o").get(0).getElementsByTag("a").get(0).text();
        List<String> tags = document.getElementsByClass("tag-link click-trigger").stream().map(Element::text).collect(Collectors.toList());
        String iframeUrl = "https:" + document.getElementsByTag("iframe").get(0).attr("src");
        Document iframeDocument = JsoupUtil.getDocument(iframeUrl, enableProxy);
        if (Objects.isNull(iframeDocument)) {
            return null;
        }
        List<String> urlList = Arrays.stream(iframeDocument.toString().split("'")).filter(text -> text.endsWith(".mp4")).map(text -> "https:" + text).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(urlList)) {
            return null;
        }
        Video video = new Video();
        video.setName(name + ".mp4");
        video.setStarNames(Collections.singletonList(startName));
        video.setTags(tags);
        video.setCreateDate(new Date());
        video.setVideoUrl(urlList.get(urlList.size() - 1));
        return video;
    }

    private void downloadVideo(Video video) {
        if (hasFilterKey(video.getName())) {
            logger.info("{},含有过滤字段", video.getName());
            return;
        }
        multithreadingDownload.videoDownload(video, null, enableProxy, thread, defaultSegmentSize);
    }

    public void download4k() {
        downloadByCategory("4k-porn");
    }

    public void downloadSexParties() {
        downloadByCategory("sex-parties");
    }

    public void downloadByCategory(String category) {
        int page = 1;
        while (true) {
            List<String> urlList = getVideoList(category, page);
            if (CollectionUtils.isEmpty(urlList)) {
                break;
            }
            urlList.forEach(url -> {
                try {
                    Video video = getVideo(url);
                    if (Objects.nonNull(video)) {
                        String date = simpleDateFormat.format(new Date());
                        video.setSavePath(savePath + fileSeparator + category + fileSeparator + date + fileSeparator + video.getName());
                        logger.info(video.getName());
                        downloadVideo(video);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            page++;
        }
    }
}
