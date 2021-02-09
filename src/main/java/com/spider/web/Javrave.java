package com.spider.web;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spider.entity.Video;
import com.spider.service.VideoService;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;
import com.spider.utils.download.MultithreadingDownload;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
public class Javrave extends BaseWeb {

    @Value("${javrave.home}")
    private String home;

    @Value("${javrave.template}")
    private String template;

    @Value("${javrave.savePath}")
    private String savePath;

    @Value("${javrave.enableProxy}")
    private Boolean enableProxy;

    @Value("${javrave.thread}")
    private Integer thread;

    @Value("${javrave.api}")
    private String api;

    @Autowired
    private VideoService videoService;

    @Autowired
    private MultithreadingDownload multithreadingDownload;

    /**
     * 获取视频的源地址列表
     *
     * @param category
     * @param page
     * @return
     */
    public List<Video> getVideoList(String category, Integer page) {
        List<Video> list = new ArrayList<>();
        String url = template.replace("@{category}", category).replace("@{page}", String.valueOf(page));
        logger.info(url);
        Document document = JsoupUtil.getDocument(url, enableProxy);
        if (Objects.isNull(document)) {
            return list;
        }
        Elements elements = document.getElementsByTag("article");
        elements.stream().forEach(element -> {
            Elements h3 = element.getElementsByTag("h3");
            if (h3.size() > 0) {
                Elements as = h3.get(0).getElementsByTag("a");
                if (as.size() > 0) {
                    Video video = new Video();
                    String source = as.get(0).attr("href");
                    String name = as.get(0).text();
                    video.setSourceUrl(source);
                    video.setName(name);
                    list.add(video);
                }
            }
        });
        return list;
    }

    /**
     * 获取视频的信息
     *
     * @param url
     * @return
     */
    public Video getVideoInfo(String url) {
        Video video = new Video();
        Document document = JsoupUtil.getDocument(url, enableProxy);
        if (Objects.isNull(document)) {
            return null;
        }
        Elements elements = document.getElementsByTag("script");
        for (Element element : elements) {
            if (element.data().contains("iframecontent")) {
                String data = element.data();
                String iframecontent = data.split("var iframecontent=\"")[1].split("\";")[0];
                Document document1 = Jsoup.parse(iframecontent.replace("\\\"", "\""));
                String src = document1.getElementById("vid_iframe").attr("src");
                String key = src.split("/")[src.split("/").length - 1];
                String json = OKHttpUtils.post(api.replace("@{key}", key), enableProxy);
                if (Objects.isNull(json)) {
                    return null;
                }
                logger.info("{}", json);
                JSONObject jsonObject = JSON.parseObject(json);
                JSONObject fileData = jsonObject.getJSONArray("data").getJSONObject(jsonObject.getJSONArray("data").size() - 1);
                String forwardUrl = fileData.getString("file");
                String label = fileData.getString("label");
                String type = fileData.getString("type");
                String videoUrl = OKHttpUtils.getRedirectUrl(forwardUrl, enableProxy);
                if (Objects.nonNull(videoUrl)) {
                    video.setVideoUrl(videoUrl);
                    video.setSourceUrl(url);
                    Elements videoInfo = document.getElementsByClass("video_title_box");
                    if (videoInfo.size() > 0) {
                        video.setFormat(type);
                        video.setQuality(label);
                        Element postMetadata = videoInfo.get(0).getElementsByClass("post-metadata").get(0);
                        video.setName(document.getElementsByClass("current").get(0).text());
                        Elements ps = postMetadata.getElementsByTag("p");
                        Map<String, String> map = new HashMap<>();
                        ps.stream().forEach(p -> {
                            String[] strArr = p.text().split(":");
                            if (strArr.length > 1) {
                                map.put(strArr[0], strArr[1].trim());
                            }
                        });
                        if (map.containsKey("Product Code")) {
                            video.setAvCode(map.get("Product Code"));
                        }
                        if (map.containsKey("Studio")) {
                            video.setStudio(map.get("Studio"));
                        }
                        if (map.containsKey("Codes") && Objects.isNull(video.getAvCode())) {
                            video.setAvCode(map.get("Codes"));
                        }
                        Elements tags = document.getElementsByTag("tags");
                        if (tags.size() > 0) {
                            Elements as = tags.get(0).getElementsByTag("a");
                            List<String> tagList = new ArrayList<>();
                            as.stream().forEach(tag -> {
                                tagList.add(tag.text());
                            });
                            video.setTags(tagList);
                        }
                    }
                } else {
                    return null;
                }
            }
        }
        logger.info(JSON.toJSONString(video));
        return video;
    }


    /**
     * 下载视频
     *
     * @param category
     */
    public void downloadVideo(String category) {
        int page = 1;
        while (true) {
            try {
                List<Video> list = getVideoList(category, page);
                list.forEach(video -> {
                    if (hasFilterKey(video.getName())) {
                        logger.info("{},含有过滤字段", video.getName());
                        return;
                    }
                    if(videoExistVerify(video)){
                        logger.info("{},已存在", video.getName());
                        return;
                    }

                    Video getVideo = getVideoInfo(video.getSourceUrl());
                    if (Objects.isNull(getVideo)) {
                        return;
                    }
                    String date = simpleDateFormat.format(new Date());
                    getVideo.setName(FileUtils.repairPath(getVideo.getName()) + ".mp4");
                    String videoSavePath = savePath + category + fileSeparator + date + fileSeparator + getVideo.getName();
                    getVideo.setSavePath(videoSavePath);
                    multithreadingDownload.videoDownload(getVideo, null, enableProxy, thread, defaultSegmentSize);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(page>1300){
                break;
            }
            page++;
        }
    }

    /**
     * 下载无码视频
     */
    public void downloadUncensored() {
        downloadVideo("uncensored");
    }
}
