package com.spider.web;

import com.spider.entity.Video;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;
import io.lindstrom.m3u8.model.MasterPlaylist;
import io.lindstrom.m3u8.model.Variant;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class Javhuge extends BaseWeb {

    @Value("${javhuge.template}")
    private String template;

    public List<Map<String, String>> getVideoList(String category, Integer page) {
        String url = template.replace("@{category}", category).replace("@{page}", String.valueOf(page));
        Document document = JsoupUtil.getDocument(url, enableProxy);
        if (Objects.isNull(document)) {
            return null;
        }
        return document.getElementsByClass("myui-vodlist__box").stream().map(element -> {
            if (element.getElementsByTag("a").size() != 0) {
                Map<String, String> map = new HashMap<>();
                String videoUrl = home + element.getElementsByTag("a").get(0).attr("href");
                String title = element.getElementsByTag("a").get(0).attr("title");
                map.put("url", videoUrl);
                map.put("title", title);
                return map;
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Map<String, String> getVideoInfo(String url) {
        Document document = JsoupUtil.getDocument(url, enableProxy);
        if (Objects.isNull(document)) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        map.put("name", document.getElementsByClass("myui-vodlist__thumb wide  picture").get(0).attr("title"));
        Elements elements = document.getElementsByClass("btn btn-warm");
        if (CollectionUtils.isEmpty(elements)) {
            return null;
        }
        String videPlayUrl = home + elements.get(0).attr("href");
        document = JsoupUtil.getDocument(videPlayUrl, enableProxy);
        Element script = document.getElementsByTag("script").stream().filter(element -> element.data().contains("ura")).findFirst().get();
        map.put("url", script.data().split("\"")[1]);
        return map;
    }

    public void download(String category) {
        int page = 1;
        while (true) {
            try {
                List<Map<String, String>> vidoeList = getVideoList(category, page);
                if (CollectionUtils.isEmpty(vidoeList)) {
                    continue;
                }
                vidoeList.stream().sequential().forEach(map -> {
                    String url = map.get("url");
                    String title = FileUtils.repairPath(map.get("title"));
                    Video video = new Video();
                    video.setSourceUrl(url);
                    video.setName(title);
                    if (videoExistVerify(video)) {
                        Map<String, String> videoInfo = getVideoInfo(url);
                        String path = savePath + fileSeparator + category + fileSeparator + simpleDateFormat.format(new Date()) + fileSeparator + videoInfo.get("name") + ".mp4";
                        video.setName(videoInfo.get("name") + ".mp4");
                        video.setSavePath(path);
                        String m3u8Url = videoInfo.get("url");
                        String m3u8Str=  OKHttpUtils.get(m3u8Url,enableProxy);
                        if(hlsDownloader.isMasterPlaylist(m3u8Str)){
                            MasterPlaylist masterPlaylist = hlsDownloader.getMasterPlaylist(m3u8Str);
                            //分辨率排序
                            List<Variant> list = masterPlaylist.variants().stream().filter(variant -> variant.resolution().isPresent()).
                                    sorted(Comparator.comparing(variant -> variant.resolution().get().height() * variant.resolution().get().width())).
                                    collect(Collectors.toList());
                            Collections.reverse(list);
                            if (org.apache.commons.collections.CollectionUtils.isEmpty(list)) {
                                list = masterPlaylist.variants();
                            }
                            if (!list.get(0).uri().startsWith("http")) {
                                if (list.get(0).uri().startsWith("/")) {
                                    String[] strs = m3u8Url.split("/");
                                    video.setVideoUrl(strs[0] + "/" + strs[1] + "/" + strs[2] + list.get(0).uri());
                                } else {
                                    video.setVideoUrl(m3u8Url.substring(0, m3u8Url.lastIndexOf("/") + 1) + masterPlaylist.variants().get(0).uri());
                                }
                            }else{
                                video.setVideoUrl(list.get(0).uri());
                            }
                        }else{
                            video.setVideoUrl(m3u8Url);
                        }
                        hlsDownloader.downloadByVideo(video, thread, enableProxy);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (page > defaultEndPage) {
                break;
            }
            page++;
        }
    }


}
