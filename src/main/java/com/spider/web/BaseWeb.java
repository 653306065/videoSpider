package com.spider.web;

import cn.hutool.crypto.digest.MD5;
import com.spider.entity.AvInfo;
import com.spider.entity.FilterRule;
import com.spider.entity.Video;
import com.spider.service.AvInfoService;
import com.spider.service.FilterRuleServcie;
import com.spider.service.VideoService;
import com.spider.utils.FileUtils;
import com.spider.utils.SpringContentUtil;
import com.spider.utils.download.HlsDownloader;
import com.spider.utils.download.MultithreadingDownload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public abstract class BaseWeb implements ApplicationRunner {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final long defaultSegmentSize = 1024 * 1024 * 2;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    protected AvInfoService avInfoService;

    @Autowired
    protected VideoService videoService;

    @Autowired
    protected MultithreadingDownload multithreadingDownload;

    @Autowired
    protected HlsDownloader hlsDownloader;

    protected String fileSeparator = File.separator;

    protected Integer defaultEndPage = 500;
    
    //配置线程数
    protected Integer thread;

    //是否开启代理
    protected Boolean enableProxy;

    //保存路径
    protected String savePath;

    //主页
    protected String home;

    protected boolean hasFilterKey(String name) {
        for(FilterRule key:FilterRuleServcie.filterRuleList){
            if (key.getEnable()) {
                if (key.getType() == FilterRule.Rule.String) {
                    if (name.contains(key.getRule()) || name.toLowerCase().contains(key.getRule().toLowerCase()) || name.toUpperCase().contains(key.getRule().toUpperCase())) {
                        logger.info("{},包含过滤规则:{}",name,key.getRule());
                        return true;
                    }
                } else if (key.getType() == FilterRule.Rule.RegExp) {
                    Pattern pattern= Pattern.compile(key.getRule(),Pattern.CASE_INSENSITIVE);
                    if (pattern.matcher(name).matches()) {
                        logger.info("{},包含过滤规则:{}",name,key.getRule());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected boolean videoExistVerify(Video video) {
        if (Objects.nonNull(video.getName()) && hasFilterKey(video.getName())) {
            return false;
        }

        if (Objects.nonNull(video.getName()) && Objects.nonNull(videoService.findOnekeyValue("name", video.getName()))) {
            logger.info("{},视频名已存在", video.getName());
            return false;
        }

        if (Objects.nonNull(video.getName()) && Objects.nonNull(videoService.findOnekeyValue("name", video.getName().toLowerCase()))) {
            logger.info("{},视频名已存在", video.getName());
            return false;
        }

        if (Objects.nonNull(video.getName()) && Objects.nonNull(videoService.findOnekeyValue("name", video.getName().toUpperCase()))) {
            logger.info("{},视频名已存在", video.getName());
            return false;
        }

        if (Objects.nonNull(video.getSourceUrl()) && Objects.nonNull(videoService.findOnekeyValue("sourceUrl", video.getSourceUrl()))) {
            logger.info("{},视频地址已存在", video.getName());
            return false;
        }
        if (Objects.nonNull(video.getAvCode()) && Objects.nonNull(videoService.findOnekeyValue("avCode", video.getAvCode()))) {
            logger.info("{},{},avCode已存在", video.getName(), video.getAvCode());
            return false;
        }

        if (Objects.nonNull(video.getName())) {
            List<String> keyList = FileUtils.getSearchKeyList(video.getName());
            for (String key : keyList) {
                if (Objects.nonNull(videoService.findOnekeyValue("avCode", key))) {
                    logger.info("{},{},avCode已存在", video.getName(), key);
                    return false;
                }
                AvInfo avInfo = avInfoService.findOnekeyValue("code", key);
                if(Objects.nonNull(avInfo)&&avInfo.isHasVideo()){
                    logger.info("视频已存在,{}", avInfo.getVideoSavePath());
                    return false;
                }
            }
        }

        if(Objects.nonNull(video.getName())){
            for (Map.Entry<String, List<String>> entry : AvInfoService.codeTransformMap.entrySet()) {
                for (String code : entry.getValue()) {
                    if (video.getName().contains(code)||video.getName().toLowerCase().contains(code.toLowerCase())||video.getName().toUpperCase().contains(code.toUpperCase())) {
                        Video findVideo= videoService.findOnekeyValue("avCode",entry.getKey());
                        if(Objects.nonNull(findVideo)){
                            logger.info("{},acCode已存在,{}", video.getName(),findVideo.getSavePath());
                            return  false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void run(ApplicationArguments args) {
        Environment environment = SpringContentUtil.getApplicationContext().getEnvironment();
        String name = this.getClass().getSimpleName().toLowerCase();
        if (environment.containsProperty(name + ".thread")) {
            thread = Integer.valueOf(environment.getProperty(name + ".thread"));
        }
        if (environment.containsProperty(name + ".enableProxy")) {
            enableProxy = Boolean.valueOf(environment.getProperty(name + ".enableProxy"));
        }
        if (environment.containsProperty(name + ".home")) {
            home = environment.getProperty(name + ".home");
        }
        if (environment.containsProperty(name + ".savePath")) {
            savePath = environment.getProperty(name + ".savePath");
        }
    }


    public Integer getThread() {
        return thread;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }

    public Boolean getEnableProxy() {
        return enableProxy;
    }

    public void setEnableProxy(Boolean enableProxy) {
        this.enableProxy = enableProxy;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }
}
