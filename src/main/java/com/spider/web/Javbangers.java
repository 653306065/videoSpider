package com.spider.web;

import com.spider.entity.Video;
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

    @Value("#{'${javbangers.filterKey}'.split(',')}")
    private List<String> filterKey;

    @Autowired
    private MultithreadingDownload multithreadingDownload;

    public List<Video> getVideoListByUrl(String categories, Integer page) {
        String listUrl = template.replace("@{categories}", categories).replace("@{page}", String.valueOf(page)).replace("@{time}", String.valueOf(System.currentTimeMillis()));
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
            logger.info("videoFileUrl:{}",videoFileUrl);
            return video;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void downloadUncensored() {
        int page = 1;
        while (true) {
            try {
                List<Video> videoList = getVideoListByUrl("uncensored", page);
                videoList = videoList.stream().filter(v -> Objects.nonNull(v.getSourceUrl())).collect(Collectors.toList());
                A: for(Video video:videoList){
                    Video findVideo= videoService.findOnekeyValue("sourceUrl",video.getSourceUrl());
                    if(Objects.nonNull(findVideo)){
                        logger.info("{},已存在",video.getSourceUrl());
                        continue;
                    }
                    video=getVideoInfo(video);
                    String date = simpleDateFormat.format(new Date());
                    String videoSavePath = savePath + "uncensored" + File.separator + date + File.separator + video.getName();
                    video.setSavePath(videoSavePath);
                    for(String key:filterKey){
                        if(video.getName().contains(key)){
                            logger.info("{},的名称有过滤字段",video.getName());
                            continue A;
                        }
                    }
                    multithreadingDownload.videoDownload(video, null, enableProxy, thread, defaultSegmentSize);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            page++;
        }
    }

    public String setCookie(String setCookie) {
        Map<String, String> setCookieMap = cookieToMap(setCookie);
        Map<String, String> cookieMap = cookieToMap(cookie);
        for (Map.Entry<String, String> entry : setCookieMap.entrySet()) {
            if(cookieMap.containsKey(entry.getKey())){
                cookieMap.put(entry.getKey(),entry.getValue());
            }
        }
        String str="";
        for(Map.Entry<String,String> entry:cookieMap.entrySet()){
            str=str+entry.getKey()+"="+entry.getValue()+";";
        }
        cookie=str;
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
