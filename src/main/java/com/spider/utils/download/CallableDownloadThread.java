package com.spider.utils.download;

import com.spider.utils.OKHttpUtils;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

public class CallableDownloadThread implements Callable<Boolean> {

    private Logger logger = LoggerFactory.getLogger(CallableDownloadThread.class);

    private String httpUrl;

    private Map<String, String> header;

    private Boolean isProxy;

    private long startByte;

    private long endByte;

    private File file;

    private long pieceDownloadByte = 0;

    private AtomicLong downloadByte;

    private Integer errorTime = 0;

    public CallableDownloadThread(String httpUrl, Map<String, String> header, Boolean isProxy, long startByte, long endByte, File file, AtomicLong downloadByte) {
        this.httpUrl = httpUrl;
        this.header = header;
        this.isProxy = isProxy;
        this.startByte = startByte;
        this.endByte = endByte;
        this.file = file;
        this.downloadByte = downloadByte;
    }


    @Override
    public Boolean call() throws Exception {
        return download();
    }


    public Boolean download() {
        while (true) {
            RandomAccessFile raf = null;
            try {
                Map<String, String> newheader = new HashMap<String, String>();
                if (header != null) {
                    newheader.putAll(header);
                }
                newheader.put("range", "bytes=" + startByte + "-" + endByte);
                if (errorTime == 5) {
                    MultithreadingDownload.downloadStatusMap.put(httpUrl, false);
                    logger.info("错误次数过多停止下载,{}", errorTime);
                    return false;
                }
                Response response = OKHttpUtils.getResponse(httpUrl, newheader, isProxy);
                if (Objects.isNull(response) || !response.isSuccessful()) {
                    logger.info("网络连接失败");
                    errorTime++;
                    continue;
                }
                byte[] bytes = new byte[1024 * 1024 * 2];
                raf = new RandomAccessFile(file, "rw");
                raf.seek(startByte);
                while (true && MultithreadingDownload.downloadStatusMap.get(httpUrl)) {
                    int i = response.body().byteStream().read(bytes);
                    if (i == -1) {
                        break;
                    }
                    downloadByte.getAndAdd(i);
                    pieceDownloadByte = pieceDownloadByte + i;
                    raf.write(bytes, 0, i);
                }
                raf.close();
                response.close();
                return true;
            } catch (Exception e) {
                logger.error(e.getMessage());
                errorTime++;
                downloadByte.getAndAdd(0 - pieceDownloadByte);
                pieceDownloadByte = 0;
            } finally {
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
