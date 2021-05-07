package com.spider.web;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.alibaba.fastjson.JSONArray;
import com.spider.entity.Video;
import com.spider.utils.OKHttpUtils;
import io.lindstrom.m3u8.model.MasterPlaylist;
import okhttp3.Response;
import org.apache.commons.collections.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
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

    public Video getVideoByUrl(String url) throws IOException {
        Response response = OKHttpUtils.getResponse(url, null, enableProxy);
        if (Objects.isNull(response) || !response.isSuccessful()) {
            return null;
        }
        String cookie = String.join(";", response.headers().values("set-cookie"));
        Document document = Jsoup.parse(response.body().string());
        if (Objects.isNull(document)) {
            return null;
        }
        Element element = document.getElementById("player");
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
        video.setName(FileUtils.repairPath(document.getElementsByClass("inlineFree").get(0).text()));
        String js = element.getElementsByTag("script").get(0).html().split("loadScriptUniqueId")[0];
        String jsonUrl = getScriptJsonString(js);
        String videoJson = OKHttpUtils.get(jsonUrl, new HashMap<>() {{
            put("cookie", cookie);
        }}, enableProxy);
        JSONArray jsonObject = JSON.parseArray(videoJson);
        if (!CollectionUtils.isEmpty(jsonObject)) {
            String quality = jsonObject.getJSONObject(jsonObject.size() - 1).getString("quality");
            String format = jsonObject.getJSONObject(jsonObject.size() - 1).getString("format");
            String videoUrl = jsonObject.getJSONObject(jsonObject.size() - 1).getString("videoUrl");
            video.setQuality(quality);
            video.setFormat(format);
            video.setVideoUrl(videoUrl);
            return video;
        } else {
            return null;
        }
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
            logger.info(url);
            List<String> list = getChannelsVideoList(url);
            list.forEach(value -> {
                try {
                    Video video = getVideoByUrl(value);
                    if (Objects.nonNull(video)) {
                        downloadVideo(video, "channels", channels);
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
                        if (Objects.nonNull(video)) {
                            downloadVideo(video, key, null);
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
                    if (Objects.nonNull(video)) {
                        downloadVideo(video, categoriesName, null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            page++;
        }
    }

    public void downloadCreampie_gangbang() {
        downloadIncategories("creampie", "gangbang");
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
                    if (Objects.nonNull(video)) {
                        downloadVideo(video, categories, incategories);
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

    public String getScriptJsonString(String js) {
        js = js.split("playerObjList")[0];
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("graal.js");
        try {
            Object value = engine.eval(js);
            return String.valueOf(value);
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void downloadVideo(Video video, String categories, String incategories) {
        video.setName(video.getName() + ".mp4");
        String date = simpleDateFormat.format(new Date());
        if (StringUtils.hasText(categories) && StringUtils.hasText(incategories)) {
            video.setSavePath(savePath + categories + "+" + incategories + File.separator + date + File.separator + video.getName());
        }
        if (StringUtils.hasText(categories) && !StringUtils.hasText(incategories)) {
            video.setSavePath(savePath + categories + File.separator + date + File.separator + video.getName());
        }
        if (StringUtils.hasText(video.getVideoUrl())) {
            if (video.getVideoUrl().contains("m3u8")) {
                MasterPlaylist masterPlaylist = hlsDownloader.getMasterPlaylist(video.getVideoUrl(), enableProxy);
                if (Objects.nonNull(masterPlaylist) && !CollectionUtils.isEmpty(masterPlaylist.variants())) {
                    if (masterPlaylist.variants().get(0).uri().startsWith("http")) {
                        video.setVideoUrl(masterPlaylist.variants().get(0).uri());
                    } else {
                        video.setVideoUrl(video.getVideoUrl().substring(0, video.getVideoUrl().lastIndexOf("/") + 1) + masterPlaylist.variants().get(0).uri());
                    }
                    hlsDownloader.downloadByVideo(video, thread, enableProxy);
                }
            } else {
                multithreadingDownload.videoDownload(video, null, enableProxy, thread, defaultSegmentSize);
            }
        }
    }

}
