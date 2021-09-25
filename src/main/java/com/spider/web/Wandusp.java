package com.spider.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spider.constant.Constant;
import com.spider.entity.Video;
import com.spider.utils.FileUtils;
import com.spider.utils.HtmlCleanerUtil;
import com.spider.utils.OKHttpUtils;
import io.lindstrom.m3u8.model.MasterPlaylist;
import io.lindstrom.m3u8.model.Variant;
import org.apache.commons.collections.CollectionUtils;
import org.htmlcleaner.TagNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
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
            Object[] titles = tagNode.evaluateXPath("//div[@class='post-content']/a/@title");
            Object[] hrefs = tagNode.evaluateXPath("//div[@class='post-content']/a/@href");
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
            Map<String, String> header = new HashMap<>() {{
                put("referer", video.getSourceUrl());
                put("user-agent", Constant.user_agent);
            }};
            String playHtml = OKHttpUtils.get(apiUrl, header, enableProxy);
            TagNode playTagNode = HtmlCleanerUtil.getTagNode(playHtml);
            Object[] scripts = playTagNode.evaluateXPath("//script/text()");
            String js = Stream.of(scripts).filter(value -> String.valueOf(value).contains("var urls")).map(String::valueOf).findFirst().get();
            String m3u8Master = js.split("\"")[1];
            String m3u8Txt = OKHttpUtils.get(m3u8Master, header, enableProxy);
            if(Objects.isNull(m3u8Txt)){
                return null;
            }
            if (!hlsDownloader.isMasterPlaylist(m3u8Txt)) {
                video.setVideoUrl(m3u8Master);
            } else {
                MasterPlaylist masterPlaylist = hlsDownloader.getMasterPlaylist(m3u8Txt);
                //分辨率排序
                List<Variant> list = masterPlaylist.variants().stream().filter(variant -> variant.resolution().isPresent()).
                        sorted(Comparator.comparing(variant -> variant.resolution().get().height() * variant.resolution().get().width())).
                        collect(Collectors.toList());
                Collections.reverse(list);
                if (CollectionUtils.isEmpty(list)) {
                    list = masterPlaylist.variants();
                }
                if (!list.get(0).uri().startsWith("http")) {
                    if (list.get(0).uri().startsWith("/")) {
                        String[] strs = m3u8Master.split("/");
                        video.setVideoUrl(strs[0] + "/" + strs[1] + "/" + strs[2] + list.get(0).uri());
                    } else {
                        video.setVideoUrl(m3u8Master.substring(0, m3u8Master.lastIndexOf("/") + 1) + masterPlaylist.variants().get(0).uri());
                    }
                }else{
                    video.setVideoUrl(list.get(0).uri());
                }
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
            if (Objects.isNull(videoList)) {
                continue;
            }
            if (Objects.nonNull(videoList) && videoList.size() == 0) {
                break;
            }
            videoList.forEach(video -> {
                if (videoExistVerify(video)) {
                    Video getVideo = getVideoInfo(video);
                    if (Objects.nonNull(getVideo) && videoExistVerify(video)) {
                        getVideo.setName(FileUtils.repairPath(getVideo.getName()) + ".mp4");
                        getVideo.setSavePath(savePath + path + fileSeparator + simpleDateFormat.format(new Date()) + fileSeparator + getVideo.getName());
                        hlsDownloader.downloadByVideo(getVideo, thread, enableProxy);
                    }
                }
            });
            page++;
        }

    }

    public void downloadUncensored() {
        downloadVideo(48, "uncensored");
    }
}
