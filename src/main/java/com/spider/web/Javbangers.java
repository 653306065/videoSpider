package com.spider.web;

import cn.hutool.core.collection.CollectionUtil;
import com.spider.entity.AvInfo;
import com.spider.entity.Video;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class Javbangers extends BaseWeb {

    @Value("${javbangers.template}")
    private String template;

    @Value("${javbangers.cookie}")
    private String cookie;

    public List<Video> getVideoListByUrl(String categories, Integer page) {
        String listUrl = template.replace("@{categories}", categories).replace("@{page}", String.valueOf(page)).replace("@{time}", String.valueOf(System.currentTimeMillis()));
        logger.info(listUrl);
        Document document = JsoupUtil.getDocument(listUrl, enableProxy);
        if (Objects.isNull(document)) {
            return null;
        }
        Elements elements = document.getElementsByClass("inf");
        return elements.stream().map(element -> {
            Video video = new Video();
            String sourceUrl = element.getElementsByTag("a").get(0).attr("href");
            String name = element.getElementsByTag("a").get(0).text();
            video.setSourceUrl(sourceUrl);
            video.setName(FileUtils.repairPath(name) + ".mp4");
            return video;
        }).collect(Collectors.toList());
    }

    public Video getVideoInfo(Video video) {
        try {
            if (Objects.isNull(video.getSourceUrl())) {
                return null;
            }
            Map<String, String> header = new HashMap<String, String>();
            header.put("cookie", cookie);
            Response response = OKHttpUtils.getResponse(video.getSourceUrl(), header, enableProxy);
            if (Objects.isNull(response)) {
                return null;
            }
            String html = response.body().string();
//            String setCookie = response.headers().get("set-cookie");
//            setCookie(setCookie);
            response.close();
            Document document = Jsoup.parse(html);
            Element element = document.getElementById("tab_video_info");
            Element info = element.getElementsByClass("info").get(0);
            Elements videodescs = info.getElementsByClass("videodesc");
            if (Objects.nonNull(videodescs.get(0))) {
                String videodescText = videodescs.get(0).text();
                video.setIntroduction(videodescText);
            }
            if (info.text().contains("https://www.javbangers.com/categories/censored/")) {
                video.setCensored(true);
            }
            Elements videoFileElements = info.getElementsByClass("btn-success");
            String htmlStr = videoFileElements.toString();
            String maxFile = "1080p";
            if (htmlStr.contains("1080p")) {
                maxFile = "1080p";
            } else if (htmlStr.contains("720p")) {
                maxFile = "720p";
            } else if (htmlStr.contains("480p")) {
                maxFile = "480p";
            } else if (htmlStr.contains("360p")) {
                maxFile = "360p";
            }
            String videoFileUrl = "";
            if (CollectionUtil.isNotEmpty(videoFileElements)) {
                for (Element videoFile : videoFileElements) {
                    String text = videoFile.text();
                    if (text.contains(maxFile)) {
                        videoFileUrl = videoFile.attr("href");
                        break;
                    }
                }
            }
            video.setQuality(maxFile);
            video.setVideoUrl(videoFileUrl);
            logger.info("videoFileUrl:{}", videoFileUrl);
            return video;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void downloadOrgy() {
        downloadVideo("orgy");
    }

    public void downloadThreesome() {
        downloadVideo("threesome");
    }

    public void downloadBukkake() {
        downloadVideo("bukkake");
    }

    public void downloadAnal() {
        downloadVideo("anal");
    }

    public void downloadSquirt() {
        downloadVideo("squirt");
    }

    public void downloadCreampie() {
        downloadVideo("creampie");
    }

    public void downloadCensored() {
        downloadVideo("censored");
    }

    public void downloadUncensored() {
        downloadVideo("uncensored");
    }

    public void downloadVideo(String categories) {
        int page = 1;
        while (true) {
            try {
                List<Video> videoList = getVideoListByUrl(categories, page);
                videoList = videoList.stream().filter(v -> Objects.nonNull(v.getSourceUrl())).collect(Collectors.toList());
                for (Video video : videoList) {
                    if (videoExistVerify(video)) {
                        video = getVideoInfo(video);
                        if (videoExistVerify(video)) {
                            if (video.getCensored()) {
                                logger.info("{},有码", video.getName());
                                continue;
                            }
                            String date = simpleDateFormat.format(new Date());
                            String videoSavePath = savePath + categories + fileSeparator + date + fileSeparator + video.getName();
                            video.setSavePath(videoSavePath);
                            multithreadingDownload.videoDownload(video, null, enableProxy, thread, defaultSegmentSize);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            page++;
        }
    }

    public Boolean findAVCode(String name) {
        List<String> list = FileUtils.getSearchKeyList(name);
        AvInfo avInfo = null;
        for (String key : list) {
            avInfo = avInfoService.findOnekeyValue("code", key);
            if (Objects.nonNull(avInfo)) {
                if (!avInfo.isHasVideo()) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    public String setCookie(String setCookie) {
        Map<String, String> setCookieMap = cookieToMap(setCookie);
        Map<String, String> cookieMap = cookieToMap(cookie);
        for (Map.Entry<String, String> entry : setCookieMap.entrySet()) {
            if (cookieMap.containsKey(entry.getKey())) {
                cookieMap.put(entry.getKey(), entry.getValue());
            }
        }
        String str = "";
        for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
            str = str + entry.getKey() + "=" + entry.getValue() + ";";
        }
        cookie = str;
        return str;
    }

    public Map<String, String> cookieToMap(String cookie) {
        Map<String, String> map = new HashMap<>();
        String[] dataArr = cookie.split(";");
        for (String data : dataArr) {
            String name = data.split("=")[0];
            String value = data.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    @Override
    public void setThread(Integer thread) {
        this.thread = thread;
    }
}
