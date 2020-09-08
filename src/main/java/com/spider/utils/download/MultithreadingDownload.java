package com.spider.utils.download;

import com.alibaba.fastjson.JSON;
import com.spider.entity.Video;
import com.spider.utils.ConsoleProgressBar;
import com.spider.utils.OKHttpUtils;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Scope("prototype")
public class MultithreadingDownload {

    public AtomicLong downloadByte = new AtomicLong(0);

    //标识下载是否继续
    public static volatile Map<String,Boolean>  downloadStatusMap=new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(MultithreadingDownload.class);

    public Boolean fileDownload(String HttpUrl, String path, Map<String, String> header, boolean isProxy, int threadNum, long segmentSize) {
        try {
            long startTime = System.currentTimeMillis();
            File file = createFile(path);
            if (Objects.isNull(file)) {
                return false;
            }
            DownloadFileInfo info = getDownloadFileInfo(HttpUrl, header, isProxy);
            if (Objects.isNull(info) || !String.valueOf(info.getResponseCode()).startsWith("20")) {
                logger.info("----获取下载信息错误----");
                return false;
            } else {
                downloadStatusMap.put(HttpUrl,true);
                logger.info(path + ",开始下载,url:" + HttpUrl);
                String fileSizeStr = getOmitValue(info.getContentLength() / 1024.0 / 1024.0,6)+ "m";
                logger.info(JSON.toJSONString(info) + ",大小" + fileSizeStr);
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                raf.setLength(info.getContentLength());
                ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
                int i = 0;
                List<Future<Boolean>> downloadResult = new ArrayList<Future<Boolean>>();
                while (true) {
                    long startByte = i * segmentSize;
                    long endByte = (i + 1) * segmentSize - 1;
                    if (endByte >= info.getContentLength()) {
                        CallableDownloadThread callableDownloadThread = new CallableDownloadThread(HttpUrl, header, isProxy, startByte, endByte, file, downloadByte);
                        Future<Boolean> future = executorService.submit(callableDownloadThread);
                        downloadResult.add(future);
                        break;
                    }
                    CallableDownloadThread callableDownloadThread = new CallableDownloadThread(HttpUrl, header, isProxy, startByte, endByte, file, downloadByte);
                    Future<Boolean> future = executorService.submit(callableDownloadThread);
                    downloadResult.add(future);
                    i++;
                }
                executorService.shutdown();
                ConsoleProgressBar cpb = new ConsoleProgressBar(100, '#');
                long tmep = 0;
                long sleepTime = 300;
                while (true) {
                    double percentage = (downloadByte.get() * 1.0) / (info.getContentLength() * 1.0) * 100.0;
                    cpb.show((int) Math.floor(percentage));
                    String speedStr = "0.0";
                    if (tmep < downloadByte.get()) {
                        double speed = ((downloadByte.get() - tmep) / 1024.0 / 1024) / (sleepTime / 1000.0);
                        speedStr=getOmitValue(speed,5)
;                    }
                    System.out.print("(" + getOmitValue(percentage,5) + "%)," +  fileSizeStr+","+speedStr+"m/s");
                    tmep = downloadByte.get();
                    Thread.sleep(sleepTime);
                    if (executorService.isTerminated()) {
                        break;
                    }
                }

                boolean result = true;
                for (Future<Boolean> future : downloadResult) {
                    if (!future.get()) {
                        result = false;
                    }
                }
                downloadByte.set(0);
                System.out.println("");
                logger.info("----" + path + ",下载完成----");
                long endTime = System.currentTimeMillis();
                logger.info("耗时:" + (endTime - startTime) / 1000 / 60.0 + "分钟");
                raf.close();
                if (!result) {
                    logger.info("{},下载失败", file.getName());
                    while (!file.delete()) {
                        logger.info("{},删除失败", file.getName());
                    }
                    logger.info("{},删除成功", file.getName());
                }
                return result;
            }
        } catch (Exception e) {
            downloadByte.set(0);
            logger.info("----下载异常----");
            new File(path).delete();
            e.printStackTrace();
            return false;
        }finally {
            downloadStatusMap.remove(HttpUrl);
        }
    }

    public String getOmitValue(double value,int index){
        if (String.valueOf(value).length() > index) {
           return  String.valueOf(value).substring(0, index);
        } else {
           return String.valueOf(value);
        }
    }

    public Boolean videoDownload(Video video, Map<String, String> header, boolean isProxy, int threadNum, long segmentSize) {
        return fileDownload(video.getVideoUrl(), video.getSavePath(), header, isProxy, threadNum, segmentSize);
    }

    private File createFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            logger.info(path + ",已存在");
            return null;
        }
        file.getParentFile().mkdirs();
        return file;
    }


    private DownloadFileInfo getDownloadFileInfo(String HttpUrl, Map<String, String> header, boolean isProxy) {
        try {
            Response response = OKHttpUtils.getResponse(HttpUrl, header, isProxy);
            DownloadFileInfo fileInfo = new DownloadFileInfo();
            if (Objects.nonNull(response.header("Content-Length"))) {
                fileInfo.setContentLength(Long.valueOf(response.header("Content-Length")));
            }
            fileInfo.setContentType(response.header("Content-Type"));
            fileInfo.setResponseCode(response.code());
            fileInfo.setEtag(response.header("etag"));
            response.body().close();
            response.close();
            return fileInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
