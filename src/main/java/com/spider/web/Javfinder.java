package com.spider.web;

import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spider.constant.Constant;
import com.spider.service.UrlRecordService;
import com.spider.utils.OKHttpUtils;
import com.spider.utils.download.MultithreadingDownload;

import okhttp3.Response;

@Service
public class Javfinder extends BaseWeb {

    private Logger logger = LoggerFactory.getLogger(Javfinder.class);

    @Value("${javfinder.category.Uncensored}")
    private String Uncensored;

    @Autowired
    private MultithreadingDownload multithreadingDownload;

    @Value("${javfinder.categoryTemplate}")
    private String categoryTemplate;

    @Value("${javfinder.ignoreCode}")
    private String ignoreCode;

    @Value("${javfinder.category.Hot}")
    private String hot;

    @Autowired
    private UrlRecordService urlRecordService;

    public List<Map<String, String>> getVideoInfoUrlList(String url) {
        List<Map<String, String>> list = new ArrayList<>();
        String html = null;
        while (true) {
            Map<String, String> header = new HashMap<>();
            header.put("cookie", getCookie());
            header.put("user-agent", Constant.user_agent);
            html = OKHttpUtils.get(url, header, enableProxy);
            logger.info(html);
            if (html != null) {
                break;
            }
        }
        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByClass("main-thumb");
        for (Element element : elements) {
            Map<String, String> map = new HashMap<String, String>();
            String videoUrl = element.attr("href");
            String name = element.attr("title");
            map.put("name", name);
            map.put("url", videoUrl);
            list.add(map);
        }
        return list;
    }

    public String getCookie() {
        Response Response = OKHttpUtils.getResponse(home, null, enableProxy);
        String cookie = Response.header("set-cookie");
        Response.close();
        return cookie;
    }

    public Map<String, String> getVideoUrl(String url) {
        Map<String, String> info = new HashMap<String, String>();
        String html = OKHttpUtils.get(url, enableProxy);
        Document document = Jsoup.parse(html);
        String name = document.getElementsByClass("wrap-meta").get(0).getElementsByTag("h1").text();
        String iframeUrl = document.getElementById("avcms_player").attr("src");
        String iframeHtml = OKHttpUtils.get(iframeUrl, enableProxy);
        Document iframeDocument = Jsoup.parse(iframeHtml);
        String dataVideo = iframeDocument.getElementById("redirector").attr("data-key");

        String code = dataVideo.split("https://playfinder.xyz/v/")[1].split("#")[0];
        String apiUrl = "https://playfinder.xyz/api/source/" + code;
        logger.info("apiUrl:{}", apiUrl);
        String json = OKHttpUtils.post(apiUrl, enableProxy);
        JSONObject jsonObject = JSON.parseObject(json);
        if (jsonObject.getBooleanValue("success")) {
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            String file = jsonArray.getJSONObject(jsonArray.size() - 1).getString("file");
            String videoUrl = OKHttpUtils.getRedirectUrl(file, enableProxy);
            logger.info("videoUrl:{}", videoUrl);
            info.put("videoUrl", OKHttpUtils.getRedirectUrl(file, enableProxy));
        }
        info.put("name", name);
        return info;
    }

    public void downloadUncensored() {
        downloadByCategory("uncensored");
    }

    public void downloadHotUncensored() {
        downloadHot("uncensored");
    }

    public void downloadThisUrl(String url, String path) {
        List<Map<String, String>> list = this.getVideoInfoUrlList(url);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (Map<String, String> videoMap : list) {
            try {
                Map<String, String> map = this.getVideoUrl(videoMap.get("url"));
                String fileUrl = map.get("videoUrl");
                String date = simpleDateFormat.format(new Date());
                String name = map.get("name").replace(":", "");
                String realPath = savePath + "\\" + path + "\\" + date + "\\" + name + ".mp4";
                multithreadingDownload.fileDownload(fileUrl, realPath, null, enableProxy, thread, defaultSegmentSize);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void downloadHot(String category) {
        int page = 1;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        while (true) {
            try {
                String url = hot.replace("@{page}", String.valueOf(page)).replace("@{category}", category);
                logger.info("videoInfoUrl:{}", url);
                List<Map<String, String>> list = this.getVideoInfoUrlList(url);
                a:
                for (Map<String, String> vidoeMap : list) {
                    String str = vidoeMap.get("url");
                    if (urlRecordService.existUrl(vidoeMap.get("url"))) {
                        continue;
                    }
                    logger.info("videoPageUrl:{}", str);
                    Map<String, String> map = this.getVideoUrl(str);
                    String fileUrl = map.get("videoUrl");
                    logger.info("fileUrl:" + fileUrl);
                    String date = simpleDateFormat.format(new Date());
                    String path = savePath + "\\hot\\" + category + "\\" + date + "\\" + map.get("name") + ".mp4";
                    multithreadingDownload.fileDownload(fileUrl, path, null, enableProxy, thread, defaultSegmentSize);
                    urlRecordService.insert(str);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            page++;
        }
    }

    public void downloadByCategory(String category) {
        int page = 1;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        while (true) {
            try {
                String url = categoryTemplate.replace("@{page}", String.valueOf(page)).replace("@{category}", category);
                logger.info("videoInfoUrl:{}", url);
                List<Map<String, String>> list = this.getVideoInfoUrlList(url);
                a:
                for (Map<String, String> vidoeMap : list) {
                    String name = vidoeMap.get("name");
                    String str = vidoeMap.get("url");
                    if (urlRecordService.existUrl(vidoeMap.get("url"))) {
                        continue;
                    }
                    for (String code : ignoreCode.split(",")) {
                        if (name.indexOf(code) != -1) {
                            urlRecordService.insert(str);
                            logger.info("continue:" + str);
                            continue a;
                        }
                    }
                    logger.info("videoPageUrl:{}", str);
                    Map<String, String> map = this.getVideoUrl(str);
                    String fileUrl = map.get("videoUrl");
                    logger.info("fileUrl:" + fileUrl);
                    String date = simpleDateFormat.format(new Date());
                    String path = savePath + "\\" + category + "\\" + date + "\\" + map.get("name") + ".mp4";
                    multithreadingDownload.fileDownload(fileUrl, path, null, enableProxy, thread, defaultSegmentSize);
                    urlRecordService.insert(str);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            page++;
        }
    }
}
