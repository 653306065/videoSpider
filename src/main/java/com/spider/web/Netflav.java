package com.spider.web;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spider.entity.Video;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class Netflav extends BaseWeb {

    @Value("${netflav.listTemplate}")
    private String listTemplate;

    @Value("${netflav.videoTemplate}")
    private String videoTemplate;

    @Value("${netflav.videoInfoApi}")
    private String videoInfoApi;

    @Value("${netflav.fileApiTemplate}")
    private String fileApiTemplate;

    public List<Video> getVideoList(String category, Integer page) {
        String listUrl = listTemplate.replace("@{category}", category).replace("@{page}", String.valueOf(page));
        Document document = JsoupUtil.getDocument(listUrl, enableProxy);
        if (Objects.isNull(document)) {
            return null;
        }
        JSONObject listJson = JSON.parseObject(document.getElementById("__NEXT_DATA__").data());
        JSONArray docs = listJson.getJSONObject("props").getJSONObject("initialState").getJSONObject("uncensored").getJSONArray("docs");
        if (CollectionUtil.isEmpty(docs)) {
            return null;
        }
        List<Video> videoList = new ArrayList<>();
        for (int i = 0; i < docs.size(); i++) {
            JSONObject info = docs.getJSONObject(i);
            String videoId = info.getString("videoId");
            String videoUrl = videoTemplate.replace("@{videoId}", videoId);
            Document videoDocument = JsoupUtil.getDocument(videoUrl, enableProxy);
            if (Objects.isNull(videoDocument)) {
                continue;
            }
            JSONObject videoJson = JSON.parseObject(videoDocument.getElementById("__NEXT_DATA__").data());
            JSONObject videoInfo = videoJson.getJSONObject("props").getJSONObject("initialState").getJSONObject("video").getJSONObject("data");
            Video video = new Video();
            video.setName(videoInfo.getString("title"));
            try {
                video.setPubDate(simpleDateFormat.parse(videoInfo.getString("videoDate")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            video.setCategories(Collections.singletonList(videoInfo.getString("category")));
            video.setFormat("mp4");
            video.setSourceUrl(videoUrl);
            video.setIntroduction(videoInfo.getString("description"));
            String src = videoInfo.getString("src");
            video.setVideoUrl(src);
            video.setTags(videoInfo.getJSONArray("tags").stream().map(Objects::toString).collect(Collectors.toList()));
            video.setStarNames(videoInfo.getJSONArray("actors").stream().map(Objects::toString).collect(Collectors.toList()));
            videoList.add(video);
        }
        return videoList;
    }

    public void downloadVideo(String category) {
        int page = 1;//前面页数为其他格式
        while (true) {
            List<Video> videoList = getVideoList(category, page);
            if (CollectionUtil.isEmpty(videoList)) {
                continue;
            }
//            if(page>defaultEndPage){
//               break;
//            }
            videoList.forEach(video -> {
                video.setName(FileUtils.repairPath(video.getName()) + ".mp4");
                String path = this.savePath + category + fileSeparator + simpleDateFormat.format(new Date()) + fileSeparator + video.getName();
                video.setSavePath(path);
                if (hasFilterKey(video.getName())) {
                    logger.info("{},有过滤字段", video.getName());
                    return;
                }
                video.setVideoUrl(getFileUrl(video.getVideoUrl()));
                if (Objects.isNull(video.getVideoUrl())) {
                    return;
                }
                multithreadingDownload.videoDownload(video, null, enableProxy, thread, defaultSegmentSize);
            });
            page++;
        }
    }

    public String getFileUrl(String src) {
        if (src.contains("www.avple.video")) {
            String key = src.replace(src.substring(0, src.lastIndexOf("/") + 1), "");
            String fileApiUrl = fileApiTemplate.replace("@{key}", key);
            String fileJsonStr = OKHttpUtils.post(fileApiUrl, enableProxy);
            if (StringUtil.isBlank(fileJsonStr)) {
                return null;
            }
            JSONObject fileJson = JSON.parseObject(fileJsonStr);
            JSONArray data = fileJson.getJSONArray("data");
            String fileUrl = data.getJSONObject(data.size() - 1).getString("file");
            String realFileUrl = OKHttpUtils.getRedirectUrl(fileUrl, enableProxy);
            if (StringUtil.isBlank(realFileUrl)) {
                return null;
            }
            return realFileUrl;

        }
        return null;
    }

    public void downloadUncensoredVideo() {
        downloadVideo("uncensored");
    }

    public Integer getThread() {
        return thread;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }
}
