package com.spider.web;

import com.spider.service.AvInfoService;
import com.spider.service.VideoService;
import com.spider.utils.download.MultithreadingDownload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseWeb {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final long defaultSegmentSize = 1024 * 1024 * 2;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Value("${filterKey}")
    protected List<String> filterKey;

    @Autowired
    protected AvInfoService avInfoService;

    @Autowired
    protected VideoService videoService;

    @Autowired
    MultithreadingDownload multithreadingDownload;

    protected String fileSeparator = File.separator;

    protected Integer defaultEndPage=500;

    @PostConstruct
    public void initFilterKey() {
        filterKey.addAll(filterKey.stream().map(String::toLowerCase).collect(Collectors.toList()));
        filterKey.addAll(filterKey.stream().map(String::toUpperCase).collect(Collectors.toList()));
    }

    protected boolean hasFilterKey(String name) {
        for (String key : filterKey) {
            if (name.contains(key)) {
                return true;
            }
        }
        return false;
    }
}
