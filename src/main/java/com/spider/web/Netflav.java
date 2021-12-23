package com.spider.web;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spider.entity.Video;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;
import org.apache.commons.lang3.StringUtils;
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

    public List<Video> getVideoList(String category,String genre, Integer page) {
        String listUrl = listTemplate.replace("@{category}", category).replace("@{page}", String.valueOf(page));
        if(StringUtils.isNotBlank(genre)){
            listUrl=listUrl+"&genre="+genre;
        }
        logger.info(listUrl);
        Document document = JsoupUtil.getDocument(listUrl, enableProxy);
        if (Objects.isNull(document)) {
            return null;
        }
        if(Objects.isNull(document.getElementById("__NEXT_DATA__"))){
            return null;
        }
        JSONObject listJson = JSON.parseObject(document.getElementById("__NEXT_DATA__").data());
        JSONArray docs = listJson.getJSONObject("props").getJSONObject("initialState").getJSONObject("uncensored").getJSONArray("docs");
        if (CollectionUtil.isEmpty(docs)) {
            return null;
        }

        List<JSONObject> jsonObjectList=new ArrayList<>();
        for (int i = 0; i < docs.size(); i++) {
            JSONObject info = docs.getJSONObject(i);
            jsonObjectList.add(info);
        }

       return jsonObjectList.parallelStream().map(info->{
            String videoId = info.getString("videoId");
            String videoUrl = videoTemplate.replace("@{videoId}", videoId);
            Video video=new Video();
            video.setAvCode(info.getString("code"));
            video.setName(info.getString("title"));
            if(videoExistVerify(video)){
                Document videoDocument = JsoupUtil.getDocument(videoUrl, enableProxy);
                if (Objects.nonNull(videoDocument)) {
                    if(Objects.nonNull(videoDocument.getElementById("__NEXT_DATA__"))){
                        JSONObject videoJson = JSON.parseObject(videoDocument.getElementById("__NEXT_DATA__").data());
                        JSONObject videoInfo = videoJson.getJSONObject("props").getJSONObject("initialState").getJSONObject("video").getJSONObject("data");
                        if(Objects.nonNull(videoInfo)){
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
                            return video;
                        }
                    }
                }
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void downloadVideo(String category, String genre) {
        int page = 119;//前面页数为其他格式
        while (true) {
            List<Video> videoList = getVideoList(category,genre, page);
            if (Objects.isNull(videoList)) {
                continue;
            }
            videoList.forEach(video -> {
                video.setName(FileUtils.repairPath(video.getName()) + ".mp4");
                String path = this.savePath + category + fileSeparator + simpleDateFormat.format(new Date()) + fileSeparator + video.getName();
                if (StringUtils.isNotBlank(genre)) {
                    path = this.savePath + category + fileSeparator + genre + fileSeparator + simpleDateFormat.format(new Date()) + fileSeparator + video.getName();
                }
                video.setSavePath(path);
                if (videoExistVerify(video)) {
                    try {
                        video.setVideoUrl(getFileUrl(video.getVideoUrl()));
                        if (Objects.nonNull(video.getVideoUrl())) {
                            multithreadingDownload.videoDownload(video, null, enableProxy, thread, defaultSegmentSize);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            page--;
        }
    }

    public String getFileUrl(String src) {
        if (src.contains("www.avple.video")||src.contains("mm9842.com")) {
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

    public void downloadUncensoredVideo(String genre) {
        downloadVideo("uncensored",genre);
    }
}
