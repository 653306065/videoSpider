
package com.spider.utils.download;

import com.spider.utils.FileUtils;
import com.spider.utils.OKHttpUtils;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Builder
public class HlsDownloader {

    private Logger logger = LoggerFactory.getLogger(HlsDownloader.class);

    public String m3u8Url;

    private List<String> tsListUrl;

    private Integer threadQuantity = 10;

    private String savePath;

    private boolean isProxy = false;

    private String rootUrl;

    private ExecutorService executorService;

    private Map<String, String> tempFileMap = new ConcurrentHashMap<String, String>();

    public void download() {
        if (StringUtils.isBlank(rootUrl)) {
            rootUrl = m3u8Url.substring(0, m3u8Url.lastIndexOf("/") + 1);
        }
        tsListUrl = getTsList();
        if (CollectionUtils.isEmpty(tsListUrl)) {
            logger.info("无法获取到TS列表");
            return;
        }
        buildTask();
        executorService.shutdown();
        while (true) {
            if (executorService.isTerminated()) {
                break;
            }
            try{
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(tempFileMap.size()==tsListUrl.size()){

        }else{
            logger.info("分块下载失败");
        }
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
        executorService = Executors.newFixedThreadPool(threadQuantity);
        AtomicInteger index = new AtomicInteger(0);
        tsListUrl.forEach(ts -> {
            executorService.execute(() -> {
                byte[] bytes = OKHttpUtils.getBytes(rootUrl+ts, isProxy);
                String tempPath = savePath + "/temp/" + index.get() + ".ts";
                FileUtils.byteToFile(bytes, tempPath);
                tempFileMap.put(ts, tempPath);
                logger.info("{}/{},{}完成下载", tempFileMap.size(), tsListUrl.size(), tempPath);
            });
            index.getAndIncrement();
        });
    }

    private void mergeFile() {

    }

    public static void main(String[] args) {
        String m3u8 = "https://dv-h.phncdn.com/hls/videos/202011/01/365874011/,1080P_4000K,720P_4000K,480P_2000K,240P_400K,_365874011.mp4.urlset/index-f1-v1-a1.m3u8?ttl=1610446316&l=0&hash=5a3550b8f9915b9a3802a64858ab98fe";
        HlsDownloader.builder().m3u8Url(m3u8).build().download();
    }
}