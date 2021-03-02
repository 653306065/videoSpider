package com.spider.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ThreadPoolExecutor;

public abstract class BaseController {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected ThreadPoolExecutor threadPoolExecutor;
}
