package com.spider.web;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spider.entity.Video;
import com.spider.utils.FileUtils;
import com.spider.utils.OKHttpUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class Javfull extends BaseWeb {

    @Value("${javfull.home}")
    private String home;

    @Value("${javfull.template}")
    private String template;

    @Value("${javfull.savePath}")
    private String savePath;

    @Value("${javfull.enableProxy}")
    private Boolean enableProxy;

    @Value("${javfull.thread}")
    private Integer thread;

    @Value("${javfull.api}")
    private String api;

    public List<Video> getVideoList(String category, Integer page) {
        List<Video> videoList = new ArrayList<>();
        try {
            String url = template.replace("@{category}", category).replace("@{page}", String.valueOf(page));
            String html = OKHttpUtils.get(url, enableProxy);
            HtmlCleaner hc = new HtmlCleaner();
            TagNode tn = hc.clean(html);
            String videoXpath = "//div[@class='post thumb-border']";
            Object[] objects = tn.evaluateXPath(videoXpath);
            String nameXpath = "//div[@class='post-des']/text()";
            String urlXpath = "//a[@class='hover-posts']/@href";
            for (Object object : objects) {
                Video video = new Video();
                TagNode tagNode = (TagNode) object;
                Object[] name = tagNode.evaluateXPath(nameXpath);
                Object[] urldata = tagNode.evaluateXPath(urlXpath);
                video.setSourceUrl(urldata[0].toString());
                video.setName(name[0].toString());
                videoList.add(video);
            }
            return videoList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoList;
    }

    public Video getVideo(Video video) {
        try {
            String html = OKHttpUtils.get(video.getSourceUrl(), enableProxy);
            HtmlCleaner hc = new HtmlCleaner();
            TagNode tn = hc.clean(html);
            Object[] objects = tn.evaluateXPath("//div[@class='play-button']/@data-link");
            String dataLink = objects[0].toString();
            String key = dataLink.substring(dataLink.lastIndexOf("/") + 1);
            String json = OKHttpUtils.post(api.replace("@{key}", key), enableProxy);
            JSONObject jsonObject = JSON.parseObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            String url = "";
            for (int i = 0; i < jsonArray.size(); i++) {
                url = jsonArray.getJSONObject(i).getString("file");
            }
            String videoUrl = OKHttpUtils.getRedirectUrl(url, enableProxy);
            video.setVideoUrl(videoUrl);
            return video;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void downloadByCategory(String category) {
        int i = 0;
        while (true) {
            List<Video> videoList = getVideoList(category, i);
            if (CollectionUtil.isEmpty(videoList)) {
                break;
            }
            videoList.forEach(video -> {
                if (hasFilterKey(video.getName())) {
                    return;
                }
                Video getVideo = getVideo(video);
                getVideo.setName(FileUtils.repairPath(video.getName()) + ".mp4");
                String path = this.savePath + category + fileSeparator + simpleDateFormat.format(new Date()) + fileSeparator + video.getName();
                getVideo.setSavePath(path);
                multithreadingDownload.videoDownload(getVideo, null, enableProxy, thread, defaultSegmentSize);
            });
            i++;
        }
    }
}
