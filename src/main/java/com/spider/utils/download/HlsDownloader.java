package com.spider.utils.download;

import com.spider.entity.Video;
import com.spider.utils.FileUtils;
import com.spider.utils.OKHttpUtils;
import io.lindstrom.m3u8.model.MasterPlaylist;
import io.lindstrom.m3u8.model.MediaPlaylist;
import io.lindstrom.m3u8.model.MediaSegment;
import io.lindstrom.m3u8.parser.MasterPlaylistParser;
import io.lindstrom.m3u8.parser.MediaPlaylistParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Scope("prototype")
public class HlsDownloader {

    private Logger logger = LoggerFactory.getLogger(HlsDownloader.class);

    //M3U8url
    public String m3u8Url;

    //TS 列表
    private MediaPlaylist mediaPlaylist;

    //线程数
    private int threadQuantity = 30;

    //保存地址
    private String savePath;

    //收费使用代理
    private boolean isProxy = false;

    //根路径
    private String rootUrl;

    //线程池
    private ExecutorService executorService;

    //TS列表的临时文件位置
    private Map<String, String> tempFileMap = new ConcurrentHashMap<>();

    //重试次数
    private Integer time = 5;

    //任务状态
    private volatile boolean taskStatus;

    //播放列表转化器
    private MasterPlaylistParser masterPlaylistParser = new MasterPlaylistParser();

    //TS列表转换器
    private MediaPlaylistParser mediaPlaylistParser = new MediaPlaylistParser();

    private boolean download() {
        taskStatus=true;
        tempFileMap.clear();
        if (StringUtils.isNotBlank(m3u8Url)) {
            rootUrl = m3u8Url.substring(0, m3u8Url.lastIndexOf("/") + 1);
        }
        getTsList();
        if (Objects.isNull(mediaPlaylist)) {
            logger.info("无法获取到TS列表");
            return false;
        }
        buildTask();
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (tempFileMap.size() == mediaPlaylist.mediaSegments().size()) {
            mergeFile();
            logger.info("文件合并完成");
        } else {
            logger.info("分块下载失败,{}/{}", tempFileMap.size(), mediaPlaylist.mediaSegments().size());
            return false;
        }
        deleteTemp();
        logger.info("删除临时文件");
        return true;
    }

    public MasterPlaylist getMasterPlaylist(String masterUrl, Boolean isProxy) {
        String content = OKHttpUtils.get(masterUrl, isProxy);
        if (Objects.isNull(content)) {
            return null;
        }
        try {
            return masterPlaylistParser.readPlaylist(content);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Boolean download(String m3u8Url, String savePath, Integer threadQuantity, Boolean isProxy) {
        if (Objects.isNull(m3u8Url) || Objects.isNull(savePath) || Objects.isNull(threadQuantity) || Objects.isNull(isProxy)) {
            return false;
        }
        this.m3u8Url = m3u8Url;
        this.savePath = savePath;
        this.threadQuantity = threadQuantity;
        this.isProxy = isProxy;
        return this.download();
    }

    public Boolean downloadByVideo(Video video, Integer threadQuantity, Boolean isProxy) {
        if (Objects.isNull(video) || StringUtils.isBlank(video.getVideoUrl()) || StringUtils.isBlank(video.getSavePath()) || Objects.isNull(threadQuantity) || Objects.isNull(isProxy)) {
            return false;
        }
        this.m3u8Url = video.getVideoUrl();
        this.savePath = video.getSavePath();
        this.threadQuantity = threadQuantity;
        this.isProxy = isProxy;
        return this.download();
    }

    private void getTsList() {
        String content = OKHttpUtils.get(m3u8Url, this.isProxy);
        if (StringUtils.isBlank(content)) {
            return;
        }
        try {
            mediaPlaylist = mediaPlaylistParser.readPlaylist(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildTask() {
        executorService = Executors.newFixedThreadPool(this.threadQuantity);
        String uuid = UUID.randomUUID().toString();
        for (int i = 0; i < mediaPlaylist.mediaSegments().size(); i++) {
            final int index = i;
            executorService.execute(() -> {
                int taskTime = 0;
                while (taskStatus) {
                    if (downloadTs(uuid, index)) {
                        break;
                    }
                    if (taskTime > time) {
                        taskStatus=false;
                        logger.info("index:{},下载失败", index);
                        break;
                    }
                    taskTime++;
                }
            });
        }
    }

    private boolean downloadTs(String uuid, Integer index) {
        MediaSegment mediaSegment = mediaPlaylist.mediaSegments().get(index);
        String url = mediaSegment.uri();
        if (!url.startsWith("http")) {
            url = rootUrl + mediaSegment.uri();
        }
        byte[] bytes = OKHttpUtils.getBytes(url, isProxy);
        if (Objects.isNull(bytes)) {
            return false;
        }
        File file = new File(savePath);
        String tempPath = file.getParentFile().getAbsolutePath() + File.separator + "temp" + File.separator + uuid + File.separator + index + ".ts";
        //todo HLS解密
        FileUtils.byteToFile(bytes, tempPath);
        tempFileMap.put(mediaSegment.uri(), tempPath);
        logger.info("{}/{},{}完成下载", tempFileMap.size(), mediaPlaylist.mediaSegments().size(), tempPath);
        return true;
    }

    private void mergeFile() {
        try {
            File videoFile = new File(savePath);
            FileOutputStream fileOutputStream = new FileOutputStream(videoFile);
            byte[] bytes = new byte[1024 * 2];
            for (MediaSegment mediaSegment : mediaPlaylist.mediaSegments()) {
                File file = new File(tempFileMap.get(mediaSegment.uri()));
                FileInputStream fileInputStream = new FileInputStream(file);
                while (true) {
                    int index = fileInputStream.read(bytes);
                    if (index == -1) {
                        break;
                    }
                    fileOutputStream.write(bytes, 0, index);
                    fileOutputStream.flush();
                }
                fileInputStream.close();
            }
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteTemp() {
        FileUtils.deleteDir(new File(new ArrayList<>(tempFileMap.values()).get(0)).getParentFile().getParentFile().getAbsolutePath());
    }

    public static void main(String[] args) {
        String m3u8 = "https://dv-h.phncdn.com/hls/videos/202007/15/333255272/,1080P_4000K,720P_4000K,480P_2000K,240P_400K,_333255272.mp4.urlset/index-f3-v1-a1.m3u8?ttl=1610459084&l=0&hash=52c152513c8995afc4e7e14904e1bed5";
        //HlsDownloader.builder().m3u8Url(m3u8).build().download();
    }
}