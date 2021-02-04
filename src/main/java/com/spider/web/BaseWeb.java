package com.spider.web;

import com.spider.service.AvInfoService;
import com.spider.service.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.text.SimpleDateFormat;
import java.util.List;

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

    protected boolean hasFilterKey(String name) {
        for (String key : filterKey) {
            if (name.contains(key)) {
                return true;
            }
        }
        return false;
    }
}
