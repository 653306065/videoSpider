package com.spider.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

public abstract class BaseWeb {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final long  defaultSegmentSize=1024*1024*2;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
}
