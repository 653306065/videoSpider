
package com.spider.utils.download;

import com.spider.utils.FileUtils;
import com.spider.utils.OKHttpUtils;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Builder
public class HlsDownloader {

    @Builder.Default
    private Logger logger = LoggerFactory.getLogger(HlsDownloader.class);

    public String m3u8Url;

    private List<String> tsListUrl;

    @Builder.Default
    private int threadQuantity = 30;

    private String savePath;

    @Builder.Default
    private boolean isProxy = false;

    private String rootUrl;

    private ExecutorService executorService;

    @Builder.Default
    private Map<String, String> tempFileMap = new ConcurrentHashMap<String, String>();

    public void download() {
        if (StringUtils.isNotBlank(m3u8Url)) {
            rootUrl = m3u8Url.substring(0, m3u8Url.lastIndexOf("/") + 1);
        }
        tsListUrl = getTsList();
        if (CollectionUtils.isEmpty(tsListUrl)) {
            logger.info("无法获取到TS列表");
            return;
        }
        buildTask();
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (tempFileMap.size() == tsListUrl.size()) {
            mergeFile();
            logger.info("文件合并完成");
        } else {
            logger.info("分块下载失败,{}/{}", tempFileMap.size(), tsListUrl.size());
        }
        deleteTemp();
        logger.info("删除临时文件");
    }

    private List<String> getTsList() {
        String content = OKHttpUtils.get(m3u8Url, this.isProxy);
        if (StringUtils.isBlank(content)) {
            return null;
        }
        Pattern pattern = Pattern.compile(".*ts.*");
        Matcher ma = pattern.matcher(content);
        List<String> list = new ArrayList<String>();
        while (ma.find()) {
            list.add(ma.group());
        }
        return list;
    }

    private void buildTask() {
        executorService = Executors.newFixedThreadPool(this.threadQuantity);
        String uuid = UUID.randomUUID().toString();
        for (int i = 0; i < tsListUrl.size(); i++) {
            final int index = i;
            executorService.execute(() -> {
                int time = 0;
                while (true) {
                    if (downloadTs(uuid, index)) {
                        break;
                    }
                    if (time > 3) {
                        logger.info("index:{},下载失败", index);
                        break;
                    }
                    time++;
                }
            });
        }
    }

    private boolean downloadTs(String uuid, Integer index) {
        byte[] bytes = OKHttpUtils.getBytes(rootUrl + tsListUrl.get(index), isProxy);
        if (Objects.isNull(bytes)) {
            return false;
        }
        File file = new File(savePath);
        String tempPath = file.getParentFile().getAbsolutePath() + "/temp/" + uuid + "/" + index + ".ts";
        FileUtils.byteToFile(bytes, tempPath);
        tempFileMap.put(tsListUrl.get(index), tempPath);
        logger.info("{}/{},{}完成下载", tempFileMap.size(), tsListUrl.size(), tempPath);
        return true;
    }

    private void mergeFile() {
        try {
            File videoFile = new File(savePath);
            FileOutputStream fileOutputStream = new FileOutputStream(videoFile);
            byte[] bytes = new byte[1024 * 2];
            for (String ts : tsListUrl) {
                File file = new File(tempFileMap.get(ts));
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

    private void deleteTemp(){
        FileUtils.deleteDir(new File(tempFileMap.values().stream().collect(Collectors.toList()).get(0)).getParentFile().getParentFile().getAbsolutePath());

    }

    public static void main(String[] args) {
        String m3u8 = "https://dv-h.phncdn.com/hls/videos/202007/15/333255272/,1080P_4000K,720P_4000K,480P_2000K,240P_400K,_333255272.mp4.urlset/index-f3-v1-a1.m3u8?ttl=1610459084&l=0&hash=52c152513c8995afc4e7e14904e1bed5";
        //HlsDownloader.builder().m3u8Url(m3u8).build().download();
    }
}