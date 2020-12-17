package com.spider.web;

import com.spider.entity.AvInfo;
import com.spider.entity.Video;
import com.spider.service.AvInfoService;
import com.spider.service.VideoService;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;
import com.spider.utils.download.MultithreadingDownload;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Javbangers extends BaseWeb {

    @Value("${javbangers.home}")
    private String home;

    @Value("${javbangers.template}")
    private String template;

    @Value("${javbangers.savePath}")
    private String savePath;

    @Value("${javbangers.cookie}")
    private String cookie;

    @Value("${javbangers.enableProxy}")
    private Boolean enableProxy;

    @Value("${javbangers.thread}")
    private Integer thread;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private VideoService videoService;

    @Value("${filterKey}")
    private List<String> filterKey;

    @Autowired
    private MultithreadingDownload multithreadingDownload;

    @Autowired
    private AvInfoService avInfoService;

    public List<Video> getVideoListByUrl(String categories, Integer page) {
        String listUrl = template.replace("@{categories}", categories).replace("@{page}", String.valueOf(page)).replace("@{time}", String.valueOf(System.currentTimeMillis()));
        logger.info(listUrl);
        Document document = JsoupUtil.getDocument(listUrl, enableProxy);
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
            String html = response.body().string();
            String setCookie = response.headers().get("set-cookie");
            setCookie(setCookie);
            response.close();
            Document document = Jsoup.parse(html);
            Element element = document.getElementById("tab_video_info");
            Element info = element.getElementsByClass("info").get(0);
            Elements videodescs = info.getElementsByClass("videodesc");
            if (Objects.nonNull(video) && Objects.nonNull(videodescs.get(0))) {
                String videodescText = videodescs.get(0).text();
                video.setIntroduction(videodescText);
            }
            if (info.text().contains("https://www.javbangers.com/categories/censored/")) {
                video.setCensored(true);
            }
            Elements videoFileElements = info.getElementsByClass("btn-success");
            String htmlStr = videoFileElements.toString();
            String maxFile = "1080p";
            if (htmlStr.indexOf("1080p") != -1) {
                maxFile = "1080p";
            } else if (htmlStr.indexOf("720p") != -1) {
                maxFile = "720p";
            } else if (htmlStr.indexOf("480p") != -1) {
                maxFile = "480p";
            } else if (htmlStr.indexOf("360p") != -1) {
                maxFile = "360p";
            }
            String videoFileUrl = "";
            if (Objects.nonNull(videoFileElements)) {
                for (Element videoFile : videoFileElements) {
                    String text = videoFile.text();
                    if (text.indexOf(maxFile) != -1) {
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
                A:
                for (Video video : videoList) {
                    for (String key : filterKey) {
                        if (video.getName().contains(key) || video.getName().contains(key.toLowerCase()) || video.getName().contains(key.toUpperCase()) || video.getName().toLowerCase().contains(key) || video.getName().toUpperCase().contains(key)) {
                            logger.info("{},的名称有过滤字段", video.getName());
                            continue A;
                        }
                    }
                    Video findVideo = videoService.findOnekeyValue("sourceUrl", video.getSourceUrl());
                    if (Objects.nonNull(findVideo)) {
                        logger.info("{},已存在", video.getSourceUrl());
                        continue;
                    }
//                    if(!findAVCode(video.getName())){
//                        logger.info("{},没有avCode",video.getName());
//                        continue;
//                    }
                    video.getName();
                    video = getVideoInfo(video);
                    if (video.getCensored()) {
                        logger.info("{},有码", video.getName());
                        continue;
                    }
                    String date = simpleDateFormat.format(new Date());
                    String videoSavePath = savePath + categories + File.separator + date + File.separator + video.getName();
                    video.setSavePath(videoSavePath);
                    multithreadingDownload.videoDownload(video, null, enableProxy, thread, defaultSegmentSize);
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
}
