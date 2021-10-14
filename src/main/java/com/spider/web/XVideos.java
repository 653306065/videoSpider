package com.spider.web;

import com.spider.entity.Video;
import com.spider.utils.FileUtils;
import com.spider.utils.HtmlCleanerUtil;
import com.spider.utils.OKHttpUtils;
import io.lindstrom.m3u8.model.MasterPlaylist;
import io.lindstrom.m3u8.model.Variant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.TagNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class XVideos extends BaseWeb {

    @Value("${xvideos.channels}")
    private String channels;

    @Value("${xvideos.xpath.video}")
    private String xpathVideo;

    @Value("${xvideos.xpath.title}")
    private String xpathTitle;

    @Value("${xvideos.xpath.resolution}")
    private String xpathResolution;

    @Value("${xvideos.xpath.duration}")
    private String xpathDuration;

    @Value("${xvideos.xpath.tag}")
    private String xpathTag;


    public List<Video> channelsVideo(String channel, Integer page) {
        String url = channels.replace("@{channels}", channel).replace("@{page}", String.valueOf(page));
        String html = OKHttpUtils.post(url, getEnableProxy());
        if (Objects.isNull(html)) {
            return null;
        }
        TagNode tagNode = HtmlCleanerUtil.getTagNode(html);
        try {
            Object[] urlObjects = tagNode.evaluateXPath(xpathVideo);
            List<String> urlList = Stream.of(urlObjects).map(String::valueOf).collect(Collectors.toList());
            Object[] titleObjects = tagNode.evaluateXPath(xpathTitle);
            List<String> titleList = Stream.of(titleObjects).map(String::valueOf).collect(Collectors.toList());
            Object[] resolutionObjects = tagNode.evaluateXPath(xpathResolution);
            List<String> resolutionList = Stream.of(resolutionObjects).map(String::valueOf).collect(Collectors.toList());
            Object[] durationObjects = tagNode.evaluateXPath(xpathDuration);
            List<String> durationList = Stream.of(resolutionObjects).map(String::valueOf).collect(Collectors.toList());
            List<Video> videoList = new ArrayList<>();
            for (int i = 0; i < urlList.size(); i++) {
                Video video = new Video();
                video.setSourceUrl(home + urlList.get(i));
                video.setName(titleList.get(i));
                if (resolutionList.size() == urlList.size()) {
                    video.setQuality(resolutionList.get(0));
                }
                videoList.add(video);
            }
            return videoList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public Video getVideoInfo(Video video) {
        TagNode tagNode = HtmlCleanerUtil.getTagNode(video.getSourceUrl(), getEnableProxy());
        if (Objects.isNull(tagNode)) {
            return null;
        }
        try {
            Object[] tagObjects = tagNode.evaluateXPath(xpathTag);
            List<String> tagList = Stream.of(tagObjects).map(String::valueOf).collect(Collectors.toList());
            video.setTags(tagList);
            List<? extends TagNode> scriptList = tagNode.getElementListByName("script", true);
            String data = scriptList.stream().filter(node -> String.valueOf(node.getText()).contains("setVideoHLS")).map(TagNode::getText).findFirst().get().toString();
            String m3u8Master = data.split("setVideoHLS")[1].split("'")[1].split("'")[0];
            String m3u8urlText = OKHttpUtils.get(m3u8Master, getEnableProxy());
            String[] strArray = m3u8urlText.split("\n");
            m3u8urlText = Stream.of(strArray).map(str -> {
                if (str.contains("NAME")) {
                    return str.split(",NAME")[0];
                }
                return str;
            }).collect(Collectors.joining("\n"));
            MasterPlaylist masterPlaylist = hlsDownloader.getMasterPlaylist(m3u8urlText);
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
                    video.setVideoUrl(m3u8Master.substring(0, m3u8Master.lastIndexOf("/") + 1) + list.get(0).uri());
                }
            } else {
                video.setVideoUrl(list.get(0).uri());
            }
            return video;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void downloadChannelVideo(String channel) {
        int page = 0;
        while (true) {
            List<Video> videoList = channelsVideo(channel, page);
            if(videoList==null){
                continue;
            }
            videoList.stream().forEach(video -> {
                if(videoExistVerify(video)){
                    video=getVideoInfo(video);
                    if(Objects.nonNull(video)){
                        video.setName(FileUtils.repairPath(video.getName())+".mp4");
                        String path=savePath+channel+fileSeparator+simpleDateFormat.format(new Date())+fileSeparator+video.getName();
                        video.setSavePath(path);
                        hlsDownloader.downloadByVideo(video,getThread(),getEnableProxy());
                    }
                }
            });
        }
    }
}
