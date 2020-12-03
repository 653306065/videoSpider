package com.spider.utils.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.Proxy;
import java.util.Map;

import com.spider.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.spider.utils.OKHttpUtils;

@Component
public class ImageDownload {

    private Logger logger = LoggerFactory.getLogger(ImageDownload.class);

    public File downloadFile(String url, Map<String, String> header, String savePath, boolean isProxy) {
        File file = new File(savePath);
        if (file.exists()) {
            logger.info(savePath + ",已存在");
            return null;
        } else {
            file.getParentFile().mkdirs();
        }
        byte[] bytes = OKHttpUtils.getBytes(url, header, isProxy);
        if (bytes == null) {
            logger.info("{},下载失败", url);
            return null;
        }
        FileUtils.byteToFile(bytes,savePath);
        return new File(savePath);
    }
}
