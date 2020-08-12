package com.spider.web;

import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.spider.utils.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spider.utils.OKHttpUtils;
import com.spider.utils.download.MultithreadingDownload;

@Service
public class Eporner extends BaseWeb{

    @Value("${eporner.home}")
    private String homeUrl;

    @Value("${eporner.category.UHD}")
    private String UHDUrl;

    private final String categoryTemplate = "https://www.eporner.com/category/${category}/${page}/";

    String apiUrl = "https://www.eporner.com/xhr/video/${vid}?hash=${hash}&device=generic&domain=www.eporner.com&fallback=false&embed=false&supportedFormats=mp4&tech=Html5&_=1550911326158";

    @Value("${eporner.savePath}")
    private String savePath;

    @Value("${eporner.thread}")
    private int thread;

    @Value("${eporner.enableProxy}")
    private boolean enableProxy;

    @Autowired
    private MultithreadingDownload multithreadingDownload;

    public List<String> getDetailsList(String category, int page) {
        String realUrl = categoryTemplate.replace("${category}", category).replace("${page}", String.valueOf(page));
        String html = OKHttpUtils.get(realUrl, proxy);
        Document document = Jsoup.parse(html);
        Elements elements = document.getElementById("vidresults").getElementsByClass("hdy");
        List<String> urlList = new ArrayList<String>();
        for (Element element : elements) {
            String detailsUrl = homeUrl + element.getElementsByTag("a").get(0).attr("href");
            urlList.add(detailsUrl);
        }
        return urlList;
    }

    public Map<String, String> getvideo(String url) {
        Map<String, String> map = new HashMap<String, String>();
        String html = OKHttpUtils.get(url, proxy);
        Document document = Jsoup.parse(html);
        String name = document.getElementById("video-info").getElementsByTag("h1").get(0).text();
        Element moviexxx = document.getElementById("moviexxx");
        String js = moviexxx.getElementsByTag("script").get(2).toString();
        String vid = js.split("vid:")[1].split("'")[1];
        String hash = getHashCode(js.split("hash:")[1].split("'")[1]);
        logger.info("name:{},vid:{},hash:{}", name, vid, hash);
        String api = apiUrl.replace("${vid}", vid).replace("${hash}", hash) + System.currentTimeMillis();
        logger.info("APIUrl:{}", api);
        String json = OKHttpUtils.get(api, proxy);
        JSONObject mp4 = JSON.parseObject(json).getJSONObject("sources").getJSONObject("mp4");
        String mp4_4k_60fps = mp4.getString("2160p(4K)@60fps HD");
        String mp4_4k = mp4.getString("2160p(4K) HD");
        String mp4_2k_60fps = mp4.getString("1440p(2K)@60fps HD");
        String mp4_2k = mp4.getString("1440p(2K) HD");
        String mp4_1080p_60fps = mp4.getString("1080p@60fps HD");
        String mp4_1080p = mp4.getString("1080p HD");
        String mp4_720p_60fps = mp4.getString("720p@60fps HD");
        String mp4_720p = mp4.getString("720p HD");
        String mp4_480p = mp4.getString("480p");
        String mp4_360p = mp4.getString("360p");
        String mp4_240p = mp4.getString("240p");
        String realUrl = "";
        if (mp4_4k_60fps != null && mp4_4k_60fps.trim().length() != 0) {
            realUrl = mp4_4k_60fps;
        } else if (mp4_4k != null && mp4_4k.trim().length() != 0) {
            realUrl = mp4_4k;
        } else if (mp4_2k_60fps != null && mp4_2k_60fps.trim().length() != 0) {
            realUrl = mp4_2k_60fps;
        } else if (mp4_2k != null && mp4_2k.trim().length() != 0) {
            realUrl = mp4_2k;
        } else if (mp4_1080p_60fps != null && mp4_1080p_60fps.trim().length() != 0) {
            realUrl = mp4_1080p_60fps;
        } else if (mp4_1080p != null && mp4_1080p.trim().length() != 0) {
            realUrl = mp4_1080p;
        } else if (mp4_720p_60fps != null && mp4_720p_60fps.trim().length() != 0) {
            realUrl = mp4_720p_60fps;
        } else if (mp4_720p != null && mp4_720p.trim().length() != 0) {
            realUrl = mp4_720p;
        } else if (mp4_480p != null && mp4_480p.trim().length() != 0) {
            realUrl = mp4_480p;
        } else if (mp4_360p != null && mp4_360p.trim().length() != 0) {
            realUrl = mp4_360p;
        } else if (mp4_240p != null && mp4_240p.trim().length() != 0) {
            realUrl = mp4_240p;
        }
        map.put("name", name);
        map.put("url", JSON.parseObject(realUrl).getString("src"));
        logger.info("info:{}", JSON.toJSONString(map));
        return map;
    }

    public void videoDownload(Map<String, String> map, String category) {
        String videoUrl = map.get("url");
        String name = FileUtils.repairPath(map.get("name"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(new Date());
        String path = savePath + category + "\\" + date + "\\" + name + ".mp4";
        multithreadingDownload.fileDownload(videoUrl, path, null, proxy, thread,defaultSegmentSize);
    }

    public void download4K() {
        String category = "4k-porn";
        downloadByCategory(category);
    }

    public void download1080p() {
        String category = "hd-1080p";
        downloadByCategory(category);
    }

    public void downloadHentai() {
        String category = "hentai";
        downloadByCategory(category);
    }

    public void downloadCreampie() {
        String category = "creampie";
        downloadByCategory(category);
    }

    public void downloadDoublePenetration() {
        String category = "double-penetration";
        downloadByCategory(category);
    }

    public void downloadByCategory(String category) {
        int page = 1;
        while (true) {
            try {
                List<String> list = getDetailsList(category, page);
                for (String url : list) {
                    try {
                        Map<String, String> map = getvideo(url);
                        videoDownload(map, category);
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

    public static String getHashCode(String code) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        try {
            engine.eval(
                    "function a(a){ return parseInt(a.substring(0, 8), 16).toString(36) + parseInt(a.substring(8, 16), 16).toString(36) + parseInt(a.substring(16, 24), 16).toString(36) + parseInt(a.substring(24, 32), 16).toString(36)}");
            if (engine instanceof Invocable) {
                Invocable invoke = (Invocable) engine; // 调用merge方法，并传入两个参数
                String c = (String) invoke.invokeFunction("a", code);
                return c;
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public boolean enableProxy() {
        return enableProxy;
    }
}
