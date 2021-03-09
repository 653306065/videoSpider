package com.spider.web;

import com.spider.entity.Video;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import com.spider.utils.download.HlsDownloader;
import io.lindstrom.m3u8.model.MasterPlaylist;
import org.elasticsearch.common.recycler.Recycler;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
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
        return document.getElementsByClass("entry-title").stream().map(element -> {
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
        map.put("name", document.getElementsByClass("active").get(0).text());
        Element script = document.getElementsByTag("script").stream().filter(element -> element.data().contains("uul")).collect(Collectors.toList()).get(0);
        map.put("url", script.data().split("'")[1].replace("全集$", "").replace("在线播放$", "").replace("第01集$", "").replace("第1集$", ""));
        return map;
    }

    public void download(String category) {
        int page = 251;
        while (true) {
            try {
                List<Map<String, String>> list = getVideoList(category, page);
                list.stream().sequential().forEach(map -> {
                    String url = map.get("url");
                    String title = FileUtils.repairPath(map.get("title"));
                    if (hasFilterKey(title)) {
                        logger.info("{},包含过滤字段", title);
                        return;
                    }
                    Video video = new Video();
                    video.setSourceUrl(url);
                    video.setName(title);
                    if (videoExistVerify(video)) {
                        logger.info("{},已存在", title);
                        return;
                    }
                    Map<String, String> videoInfo = getVideoInfo(url);
                    String path = savePath + fileSeparator + category + fileSeparator + simpleDateFormat.format(new Date()) + fileSeparator + videoInfo.get("name") + ".mp4";
                    video.setName(videoInfo.get("name") + ".mp4");
                    video.setSavePath(path);
                    String masterM3u8 = videoInfo.get("url");
                    MasterPlaylist masterPlaylist = hlsDownloader.getMasterPlaylist(masterM3u8, enableProxy);
                    if (Objects.nonNull(masterPlaylist)) {
                        String m3u8 = masterPlaylist.variants().get(0).uri();
                        if (!m3u8.startsWith("http")) {
                            m3u8 = masterM3u8.substring(0, masterM3u8.lastIndexOf("/") + 1) + m3u8;
                        }
                        video.setVideoUrl(m3u8);
                        hlsDownloader.downloadByVideo(video, thread, enableProxy);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (page > defaultEndPage) {
                break;
            }
            page--;
        }
    }


}
