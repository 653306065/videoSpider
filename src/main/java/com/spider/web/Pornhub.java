package com.spider.web;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.spider.entity.Video;
import com.spider.utils.OKHttpUtils;
import com.spider.utils.download.HlsDownloader;
import io.lindstrom.m3u8.model.MasterPlaylist;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class Pornhub extends BaseWeb {

    @Value("${pornhub.searchUrl}")
    private String searchUrl;

    @Value("${pornhub.channels}")
    private String channelsTemplate;

    @Value("${pornhub.categoriesUrlByNum}")
    private String categoriesUrlByNum;

    @Value("${pornhub.incategoriesUrl}")
    private String incategoriesUrl;

    @Autowired
    private HlsDownloader hlsDownloader;

    public Video getVideoByUrl(String url) {
        Document document = JsoupUtil.getDocument(url, enableProxy);
        if(Objects.isNull(document)){
            return null;
        }
        Element element = document.getElementById("player");
        String videoId = element.attr("data-video-id");
        //分类
        Element categoriesWrapper = document.getElementsByClass("categoriesWrapper").get(0);
        //演员
        Elements starElements = document.getElementsByClass("pstar-list-btn");
        //标签
        Element tagsWrapper = document.getElementsByClass("tagsWrapper").get(0);
        //发布商
        //Element production = document.getElementsByClass("production").get(0);

        Video video = new Video();
        video.setSourceUrl(url);
        //设置分类
        if (Objects.nonNull(categoriesWrapper)) {
            Elements categoriesElements = categoriesWrapper.getElementsByTag("a");
            List<String> categoriesList = categoriesElements.stream().map(Element::text).collect(Collectors.toList());
            video.setCategories(categoriesList);
        }

        //设置演员
        List<String> starList = starElements.stream().map(Element::text).collect(Collectors.toList());
        video.setStarNames(starList);

        //设置标签
        if (Objects.nonNull(tagsWrapper)) {
            Elements tagElements = tagsWrapper.getElementsByTag("a");
            List<String> tagList = tagElements.stream().map(Element::text).collect(Collectors.toList());
            video.setTags(tagList);
        }

        //设置发布商
//        if (Objects.nonNull(production)) {
//            video.setProduction(production.text());
//        }
        String js = element.getElementsByTag("script").get(0).html().split("loadScriptUniqueId")[0];
        String json = getScriptJson(js, videoId);
        JSONObject jsonObject = JSON.parseObject(json);
        JSONObject mediaDefinitions = jsonObject.getJSONObject("mediaDefinitions");
        String name = jsonObject.getString("video_title") + ".mp4";
        video.setName(FileUtils.repairPath(name));
        int index = 0;
        while (true) {
            JSONObject info = mediaDefinitions.getJSONObject("" + index);
            if (Objects.isNull(info)) {
                break;
            }
            if ("hls".equals(info.getString("format")) && "1080".equals(info.getString("quality"))) {
                String hlsUrl = info.getString("videoUrl");
                MasterPlaylist masterPlaylist = hlsDownloader.getMasterPlaylist(hlsUrl, enableProxy);
                String videoM3u8Url = null;
                if (Objects.nonNull(masterPlaylist) && !CollectionUtils.isEmpty(masterPlaylist.variants())) {
                    if (masterPlaylist.variants().get(0).uri().startsWith("http")) {
                        videoM3u8Url = masterPlaylist.variants().get(0).uri();
                    } else {
                        videoM3u8Url = hlsUrl.substring(0, hlsUrl.lastIndexOf("/") + 1) + masterPlaylist.variants().get(0).uri();
                    }
                }
                String quality = info.getString("quality");
                String format = info.getString("format");
                video.setVideoUrl(videoM3u8Url);
                video.setQuality(quality);
                video.setFormat(format);
            }
            index++;
        }
        return video;
    }

    public List<String> getVideoList(String url) {
        List<String> list = new ArrayList<String>();
        Document document = JsoupUtil.getDocument(url, enableProxy);
        if (Objects.isNull(document)) {
            return list;
        }
        if (document.getElementById("videoSearchResult") != null) {
            Elements elements = document.getElementById("videoSearchResult").getElementsByClass("title");
            for (Element element : elements) {
                String href = element.getElementsByTag("a").attr("href");
                if (href.contains("viewkey")) {
                    list.add(home + href);
                }
            }
        } else if (document.getElementById("videoCategory") != null) {
            Elements elements = document.getElementById("videoCategory").getElementsByClass("linkVideoThumb");
            for (Element element : elements) {
                String href = element.getElementsByTag("a").attr("href");
                if (href.contains("viewkey")) {
                    list.add(home + href);
                }
            }
        } else if (document.getElementById("incategoryVideos") != null) {
            Elements elements = document.getElementById("incategoryVideos").getElementsByClass("linkVideoThumb");
            for (Element element : elements) {
                String href = element.getElementsByTag("a").attr("href");
                if (href.contains("viewkey")) {
                    list.add(home + href);
                }
            }
        }
        return list;
    }

    public List<String> getChannelsVideoList(String url) {
        List<String> list = new ArrayList<String>();
        Document document = JsoupUtil.getDocument(url, enableProxy);
        if (Objects.isNull(document)) {
            return list;
        }
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
            list.forEach(value -> {
                try {
                    Video video = getVideoByUrl(value);
                    if(Objects.isNull(video)){
                        return;
                    }
                    String date = simpleDateFormat.format(new Date());
                    String path = savePath + "channels" + fileSeparator + channels + fileSeparator + date + fileSeparator + video.getName();
                    video.setSavePath(path);
                    if (Integer.parseInt(video.getQuality()) < 720) {
                        return;
                    }
                    if (StringUtils.hasText(video.getVideoUrl())) {
                        hlsDownloader.downloadByVideo(video, thread, enableProxy);
                    }
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
                        if(Objects.isNull(video)){
                            return;
                        }
                        String date = simpleDateFormat.format(new Date());
                        String path = savePath + key + File.separator + date + File.separator + video.getName();
                        video.setSavePath(path);
                        if (Integer.parseInt(video.getQuality()) < 720) {
                            continue;
                        }
                        if (StringUtils.hasText(video.getVideoUrl())) {
                            hlsDownloader.downloadByVideo(video, thread, enableProxy);
                        }
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

    public void downloadCategoriesUrlByNum(int categoriesNum, String categoriesName) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        int page = 1;
        while (true) {
            try {
                String url = categoriesUrlByNum.replace("@{categories}", String.valueOf(categoriesNum)).replace("@{page}", String.valueOf(page));
                List<String> list = getVideoList(url);
                for (String str : list) {
                    Video video = getVideoByUrl(str);
                    if(Objects.isNull(video)){
                        return;
                    }
                    String date = simpleDateFormat.format(new Date());
                    String path = savePath + categoriesName + fileSeparator + date + fileSeparator + video.getName();
                    video.setSavePath(path);
                    if (Integer.parseInt(video.getQuality()) < 720) {
                        continue;
                    }
                    if (StringUtils.hasText(video.getVideoUrl())) {
                        hlsDownloader.downloadByVideo(video, thread, enableProxy);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            page++;
        }
    }

    public void downloadIncategories(String categories, String incategories) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        int page = 1;
        while (true) {
            try {
                String url = incategoriesUrl.replace("@{categories}", categories).replace("@{incategories}", incategories).replace("@{page}", String.valueOf(page));
                List<String> list = getVideoList(url);
                for (String str : list) {
                    Video video = getVideoByUrl(str);
                    if(Objects.isNull(video)){
                        return;
                    }
                    String date = simpleDateFormat.format(new Date());
                    String path = savePath + categories + "+" + incategories + File.separator + date + File.separator + video.getName();
                    video.setSavePath(path);
                    if (StringUtils.hasText(video.getVideoUrl())) {
                        hlsDownloader.downloadByVideo(video, thread, enableProxy);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            page++;
        }
    }

    public void download_doublePenetration_gangbang() {
        downloadIncategories("double-penetration", "gangbang");
    }

    public void download_creampie_orgy() {
        downloadIncategories("creampie", "orgy");
    }

    public void download_orgy_doublePenetration() {
        downloadIncategories("orgy", "double-penetration");
    }

    public void download_japanese_orgy() {
        downloadIncategories("japanese", "orgy");
    }


    public void download_doublePenetration_creampie() {
        downloadIncategories("double-penetration", "creampie");
    }

    public void download_gangbang_hentai() {
        downloadIncategories("gangbang", "hentai");
    }

    public void download_compilation_creampie() {
        downloadIncategories("compilation", "creampie");
    }

    public void downloadCreampie() {
        downloadCategoriesUrlByNum(15, "creampie");
    }

    public void downloadDoublePenetration_gangbang() {
        downloadCategoriesUrlByNum(15, "creampie");
    }

    public void downloadDoublePenetration() {
        downloadCategoriesUrlByNum(72, "double-penetration");
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
