package com.spider.utils.download;

import com.spider.entity.Video;
import com.spider.utils.FFmpegUtil;
import com.spider.utils.FileUtils;
import com.spider.utils.OKHttpUtils;
import io.lindstrom.m3u8.model.*;
import io.lindstrom.m3u8.parser.MasterPlaylistParser;
import io.lindstrom.m3u8.parser.MediaPlaylistParser;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Scope("prototype")
public class HlsDownloader {

    private Logger logger = LoggerFactory.getLogger(HlsDownloader.class);

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private TaskInfo taskInfo;

    //线程池
    private ExecutorService executorService;

    //播放列表转化器
    private MasterPlaylistParser masterPlaylistParser = new MasterPlaylistParser();

    //TS列表转换器
    private MediaPlaylistParser mediaPlaylistParser = new MediaPlaylistParser();

    private boolean download() {
        try {
            if (StringUtils.isNotBlank(taskInfo.m3u8Url)) {
                taskInfo.rootUrl = taskInfo.m3u8Url.substring(0, taskInfo.m3u8Url.lastIndexOf("/") + 1);
            }
            getTsList();
            if (Objects.isNull(taskInfo.mediaPlaylist)) {
                logger.info("无法获取到TS列表");
                return false;
            }
            buildTask();
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                Thread.sleep(100);
            }
            if (taskInfo.tempFileMap.size() == taskInfo.mediaPlaylist.mediaSegments().size()) {
                mergeFile();
                logger.info("文件合并完成");
            } else {
                logger.info("分块下载失败,{}/{}", taskInfo.tempFileMap.size(), taskInfo.mediaPlaylist.mediaSegments().size());
                return false;
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            deleteTemp();
            logger.info("删除临时文件");  
        }
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

    public Boolean download(String m3u8Url, String savePath, Integer threadQuantity, Boolean enableProxy) {
        if (Objects.isNull(m3u8Url) || Objects.isNull(savePath) || Objects.isNull(threadQuantity) || Objects.isNull(enableProxy)) {
            return false;
        }
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setM3u8Url(m3u8Url);
        taskInfo.setSavePath(savePath);
        taskInfo.setThreadQuantity(threadQuantity);
        taskInfo.setEnableProxy(enableProxy);
        this.taskInfo = taskInfo;
        return this.download();
    }

    public Boolean downloadByVideo(Video video, Integer threadQuantity, Boolean enableProxy) {
        if (Objects.isNull(video) || StringUtils.isBlank(video.getVideoUrl()) || StringUtils.isBlank(video.getSavePath()) || Objects.isNull(threadQuantity) || Objects.isNull(enableProxy)) {
            return false;
        }
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setM3u8Url(video.getVideoUrl());
        taskInfo.setSavePath(video.getSavePath());
        taskInfo.setThreadQuantity(threadQuantity);
        taskInfo.setEnableProxy(enableProxy);
        this.taskInfo = taskInfo;
        return this.download();
    }

    private void getTsList() {
        String content = OKHttpUtils.get(taskInfo.m3u8Url, taskInfo.enableProxy);
        if (StringUtils.isBlank(content)) {
            return;
        }
        try {
            taskInfo.mediaPlaylist = mediaPlaylistParser.readPlaylist(content);
            //绝对路径
            if (CollectionUtils.isNotEmpty(taskInfo.mediaPlaylist.mediaSegments())) {
                MediaSegment mediaSegment = taskInfo.mediaPlaylist.mediaSegments().get(0);
                if (mediaSegment.uri().startsWith("/")) {
                    String[] strs = taskInfo.rootUrl.split("/");
                    taskInfo.rootUrl = strs[0] + "/" + strs[1] + "/" + strs[2];
                }
            }
            String keyUrl = null;
            if (CollectionUtils.isNotEmpty(taskInfo.mediaPlaylist.mediaSegments())) {
                MediaSegment mediaSegment = taskInfo.mediaPlaylist.mediaSegments().get(0);
                if (mediaSegment.segmentKey().isPresent()) {
                    SegmentKey segmentKey = mediaSegment.segmentKey().get();
                    if (segmentKey.uri().isPresent()) {
                        keyUrl = segmentKey.uri().get();
                        if (!keyUrl.startsWith("http")) {
                            keyUrl = taskInfo.rootUrl + keyUrl;
                        }
                        taskInfo.key = OKHttpUtils.getBytes(keyUrl, taskInfo.enableProxy);
                        taskInfo.keyMethod = segmentKey.method();
                        if (segmentKey.iv().isPresent()) {
                            taskInfo.vi = segmentKey.iv().get().getBytes(StandardCharsets.UTF_8);
                        }
                    }
                    logger.info("key:{},method:{}", new String(taskInfo.key), taskInfo.keyMethod.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildTask() {
        executorService = Executors.newFixedThreadPool(this.taskInfo.threadQuantity);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < taskInfo.mediaPlaylist.mediaSegments().size(); i++) {
            final int index = i;
            executorService.execute(() -> {
                int taskTime = 0;
                while (taskInfo.taskStatus) {
                    if (downloadTs(uuid, index)) {
                        break;
                    }
                    if (taskTime > taskInfo.time) {
                        taskInfo.taskStatus = false;
                        logger.info("index:{},下载失败", index);
                        break;
                    }
                    taskTime++;
                }
            });
        }
    }

    private boolean downloadTs(String uuid, Integer index) {
        MediaSegment mediaSegment = taskInfo.mediaPlaylist.mediaSegments().get(index);
        String url = mediaSegment.uri();
        if (!url.startsWith("http")) {
            url = taskInfo.rootUrl + mediaSegment.uri();
        }
        byte[] bytes = OKHttpUtils.getBytes(url, taskInfo.enableProxy);
        if (Objects.isNull(bytes)) {
            return false;
        }
        File file = new File(taskInfo.savePath);
        if (Objects.nonNull(taskInfo.key) && Objects.nonNull(taskInfo.keyMethod)) {
            if (taskInfo.keyMethod == KeyMethod.AES_128 || taskInfo.keyMethod == KeyMethod.SAMPLE_AES) {
                try {
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                    SecretKeySpec keySpec = new SecretKeySpec(taskInfo.key, "AES");
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(taskInfo.vi));
                    bytes = cipher.doFinal(bytes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        String tempPath = file.getParentFile().getAbsolutePath() + File.separator + "temp" + File.separator + uuid + File.separator + index + ".ts";
        FileUtils.byteToFile(bytes, tempPath);
        taskInfo.tempFileMap.put(mediaSegment.uri(), tempPath);
        double progress = (taskInfo.tempFileMap.size() * 1.0 / taskInfo.mediaPlaylist.mediaSegments().size()) * 100;
        progress = BigDecimal.valueOf(progress).setScale(2, RoundingMode.HALF_UP).doubleValue();
        logger.info("{}/{},({}%),{},完成下载", taskInfo.tempFileMap.size(), taskInfo.mediaPlaylist.mediaSegments().size(), progress, tempPath);
        return true;
    }

    private void mergeFile() {
        String tsListPath = new File(taskInfo.tempFileMap.values().stream().findFirst().get()).getParentFile().getAbsolutePath();
        FFmpegUtil.mergeTsFile(tsListPath, taskInfo.savePath);
    }

    @Data
    public static class TaskInfo {
        //M3U8url
        private String m3u8Url;

        //TS 列表
        private MediaPlaylist mediaPlaylist;

        //加密密钥
        private byte[] key;

        //加密vi
        private byte[] vi = new byte[16];

        //加密方式
        private KeyMethod keyMethod;

        //线程数
        private int threadQuantity = 30;

        //保存地址
        private String savePath;

        //是否使用代理
        private boolean enableProxy = false;

        //根路径
        private String rootUrl;

        //TS列表的临时文件位置
        private Map<String, String> tempFileMap = new ConcurrentHashMap<>();

        //重试次数
        private Integer time = 3;

        //任务状态
        private volatile boolean taskStatus = true;
    }

    private void deleteTemp() {
        FileUtils.deleteDir(new File(new ArrayList<>(taskInfo.tempFileMap.values()).get(0)).getParentFile().getParentFile().getAbsolutePath());
    }
}