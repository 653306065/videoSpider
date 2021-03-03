package com.spider.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class BaseController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected ThreadPoolExecutor threadPoolExecutor;
}
