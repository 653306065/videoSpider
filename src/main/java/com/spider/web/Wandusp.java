package com.spider.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spider.constant.Constant;
import com.spider.entity.Video;
import com.spider.utils.FileUtils;
import com.spider.utils.HtmlCleanerUtil;
import com.spider.utils.OKHttpUtils;
import io.lindstrom.m3u8.model.MasterPlaylist;
import org.htmlcleaner.TagNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Stream;

@Component
public class Wandusp extends BaseWeb {

    @Value("${wandusp.listTemplate}")
    private String listTemplate;

    @Value("${wandusp.playerApi}")
    private String playerApi;

    public List<Video> getVideoList(Integer type, Integer page) {
        String url = listTemplate.replace("@{type}", String.valueOf(type)).replace("@{page}", String.valueOf(page));
        logger.info(url);
        TagNode tagNode = HtmlCleanerUtil.getTagNode(url, enableProxy);
        if (Objects.isNull(tagNode)) {
            return null;
        }
        List<Video> list = new ArrayList<>();
        try {
            Object[] titles = tagNode.evaluateXPath("//ul[@class='lul']/li/a/@title");
            Object[] hrefs = tagNode.evaluateXPath("//ul[@class='lul']/li/a/@href");
            for (int i = 0; i < titles.length; i++) {
                Video video = new Video();
                video.setName(String.valueOf(titles[i]));
                video.setSourceUrl(home + String.valueOf(hrefs[i]));
                list.add(video);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Video getVideoInfo(Video video) {
        try {
            logger.info(video.getSourceUrl());
            TagNode tagNode = HtmlCleanerUtil.getTagNode(video.getSourceUrl(), enableProxy);
            if (Objects.isNull(tagNode)) {
                return null;
            }
            Object[] objects = tagNode.evaluateXPath("//script/text()");
            String data = Stream.of(objects).filter(value -> String.valueOf(value).contains("player_data")).map(value -> String.valueOf(value)).findFirst().get();
            String json = data.split("=")[1];
            JSONObject jsonObject = JSON.parseObject(json);
            String url = jsonObject.getString("url");
            String apiUrl = playerApi.replace("@{url}", url).replace("@{t}", String.valueOf(Math.random()));
            String playHtml = OKHttpUtils.get(apiUrl, new HashMap<>() {{
                put("referer", video.getSourceUrl());
                put("user-agent", Constant.user_agent);
            }}, enableProxy);
            TagNode playTagNode = HtmlCleanerUtil.getTagNode(playHtml);
            Object[] scripts = playTagNode.evaluateXPath("//script/text()");
            String js = Stream.of(scripts).filter(value -> String.valueOf(value).contains("videos3.naibago.com")).map(value -> String.valueOf(value)).findFirst().get();
            String m3u8Master = js.split("\"")[1];
            MasterPlaylist masterPlaylist = hlsDownloader.getMasterPlaylist(m3u8Master, enableProxy);
            video.setVideoUrl(masterPlaylist.variants().get(0).uri());
            if (!masterPlaylist.variants().get(0).uri().startsWith("http")) {
                video.setVideoUrl(m3u8Master.substring(0, m3u8Master.lastIndexOf("/") + 1) + masterPlaylist.variants().get(0).uri());
            }
            logger.info(video.getVideoUrl());
            return video;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void downloadVideo(Integer type, String path) {
        int page = 1;
        while (true) {
            List<Video> videoList = getVideoList(type, page);
            if(Objects.isNull(videoList)){
                continue;
            }
            if (Objects.nonNull(videoList) && videoList.size() == 0) {
                break;
            }
            videoList.stream().forEach(video -> {
                Video getVideo = getVideoInfo(video);
                if (Objects.nonNull(getVideo)&&videoExistVerify(video)) {
                    getVideo.setName(FileUtils.repairPath(getVideo.getName()) + ".mp4");
                    getVideo.setSavePath(savePath + path + fileSeparator + simpleDateFormat.format(new Date()) + fileSeparator + getVideo.getName());
                    hlsDownloader.downloadByVideo(getVideo, thread, enableProxy);
                }
            });
            page++;
        }

    }

    public void downloadUncensored() {
        downloadVideo(48, "uncensored");
    }
}
