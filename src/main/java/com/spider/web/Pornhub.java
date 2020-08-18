package com.spider.web;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import com.spider.entity.Video;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import com.spider.utils.download.MultithreadingDownload;
import org.springframework.util.StringUtils;

@Service
public class Pornhub extends BaseWeb {

    @Value("${pornhub.savePath}")
    private String savePath;

    @Value("${pornhub.home}")
    private String home;

    @Value("${pornhub.searchUrl}")
    private String searchUrl;

    @Value("${pornhub.thread}")
    private int thread;

    @Value("${pornhub.channels}")
    private String channelsTemplate;

    @Value("${pornhub.enableProxy}")
    private Boolean enableProxy;

    @Autowired
    private MultithreadingDownload multithreadingDownload;

    public Video getVideoByUrl(String url) {
        Document document = JsoupUtil.getDocument(url, enableProxy);
        Element element = document.getElementById("player");
        String videoId = element.attr("data-video-id");
        //分类
        Element categoriesWrapper = document.getElementsByClass("categoriesWrapper").get(0);
        //演员
        Elements starElements = document.getElementsByClass("pstar-list-btn");
        //标签
        Element tagsWrapper = document.getElementsByClass("tagsWrapper").get(0);
        //发布商
        Element production = document.getElementsByClass("production").get(0);

        Video video = new Video();
        video.setSourceUrl(url);
        //设置分类
        if (Objects.nonNull(categoriesWrapper)) {
            Elements categoriesElements = categoriesWrapper.getElementsByTag("a");
            List<String> categoriesList = categoriesElements.stream().map(e -> {
                return e.text();
            }).collect(Collectors.toList());
            video.setCategories(categoriesList);
        }

        //设置演员
        List<String> starList = starElements.stream().map(e -> {
            return e.text();
        }).collect(Collectors.toList());
        video.setStarNames(starList);

        //设置标签
        if (Objects.nonNull(tagsWrapper)) {
            Elements tagElements = tagsWrapper.getElementsByTag("a");
            List<String> tagList = tagElements.stream().map(e -> {
                return e.text();
            }).collect(Collectors.toList());
            video.setTags(tagList);
        }

        //设置发布商
        if (Objects.nonNull(production)) {
            video.setProduction(production.text());
        }
        String js = element.getElementsByTag("script").get(0).html().split("loadScriptUniqueId")[0];
        String json = getScriptJson(js, videoId);
        JSONObject jsonObject = JSON.parseObject(json);
        JSONObject mediaDefinitions = jsonObject.getJSONObject("mediaDefinitions");
        for (Entry<String, Object> entry : mediaDefinitions.entrySet()) {
            String videoUrl = mediaDefinitions.getJSONObject(entry.getKey()).getString("videoUrl");
            String quality = mediaDefinitions.getJSONObject(entry.getKey()).getString("quality");
            String format = mediaDefinitions.getJSONObject(entry.getKey()).getString("format");
            if (!StringUtils.isEmpty(videoUrl) && "mp4".equals(format)) {
                String name = jsonObject.getString("video_title") + ".mp4";
                video.setName(FileUtils.repairPath(name));
                video.setVideoUrl(videoUrl);
                video.setQuality(quality);
                video.setFormat(format);
                return video;
            }
        }
        return null;
    }

    public List<String> getVideoList(String url) {
        List<String> list = new ArrayList<String>();
        Document document = JsoupUtil.getDocument(url, enableProxy);
        if (document.getElementById("videoSearchResult") != null) {
            Elements elements = document.getElementById("videoSearchResult").getElementsByClass("title");
            for (Element element : elements) {
                String href = element.getElementsByTag("a").attr("href");
                if (href.indexOf("viewkey") != -1) {
                    list.add(home + href);
                }
            }
        } else {
            Elements elements = document.getElementById("videoCategory").getElementsByClass("linkVideoThumb");
            for (Element element : elements) {
                String href = element.getElementsByTag("a").attr("href");
                if (href.indexOf("viewkey") != -1) {
                    list.add(home + href);
                }
            }
        }
        return list;
    }

    public List<String> getChannelsVideoList(String url) {
        List<String> list = new ArrayList<String>();
        Document document = JsoupUtil.getDocument(url, enableProxy);
        Element element = document.getElementById("showAllChanelVideos");
        Elements elements = element.getElementsByClass("videoPreviewBg");
        for (Element e : elements) {
            String href = home + e.attr("href");
            list.add(href);
        }
        return list;
    }

    public void downloadChannels(String channels) {
        int page = 1;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        while (true) {
            String url = channelsTemplate.replace("@{channelName}", channels).replace("@{page}", String.valueOf(page));
            System.out.println(url);
            List<String> list = getChannelsVideoList(url);
            list.stream().forEach(value -> {
                try {
                    Video video = getVideoByUrl(value);
                    String date = simpleDateFormat.format(new Date());
                    String path = savePath + "channels" + File.separator + channels + File.separator + date + File.separator + video.getName();
                    video.setSavePath(path);
                    if (Integer.valueOf(video.getQuality()) < 720) {
                        return;
                    }
                    multithreadingDownload.videoDownload(video, null, enableProxy, thread, defaultSegmentSize);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            if (list.size() == 0) {
                break;
            }
            page++;
        }
    }

    public void downloadSearch(String key) {
        int page = 1;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        while (true) {
            try {
                String url = searchUrl.replace("@{key}", key).replace("@{page}", String.valueOf(page));
                System.out.println(url);
                List<String> list = getVideoList(url);
                for (String str : list) {
                    try {
                        Video video = getVideoByUrl(str);
                        String date = simpleDateFormat.format(new Date());
                        String path = savePath + key + File.separator + date + File.separator + video.getName();
                        video.setSavePath(path);
                        if (Integer.valueOf(video.getQuality()) < 720) {
                            continue;
                        }
                        multithreadingDownload.videoDownload(video, null, enableProxy, thread, defaultSegmentSize);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                if (list.size() == 0) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            page++;
        }
    }

    public void downloadHmv() {
        downloadSearch("hmv");
    }

    public void downloadMMD() {
        downloadSearch("mmd");
    }

    public void downloadsfm() {
        downloadSearch("sfm");
    }

    public void download3dMonster() {
        downloadSearch("3d+monster");
    }

    public void downloadDP() {
        String url = "https://www.pornhub.com/video?c=72&page=";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        int i = 1;
        while (true) {
            try {
                List<String> list = getVideoList(url + i);
                for (String str : list) {
                    Video video = getVideoByUrl(str);
                    String date = simpleDateFormat.format(new Date());
                    String path = savePath + "dp" + File.separator + date + File.separator + video.getName();
                    video.setSavePath(path);
                    if (Integer.valueOf(video.getQuality()) < 720) {
                        continue;
                    }
                    multithreadingDownload.videoDownload(video, null, enableProxy, thread, defaultSegmentSize);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    public void downloadHentaiGangbang() {
        downloadSearch("hentai gangbang");
    }

    public String getScriptJson(String js, String videoId) {
        js = js.split("playerObjList")[0];
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        try {
            engine.eval(js);
            return JSON.toJSONString(engine.get("flashvars_" + videoId));
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }
}
