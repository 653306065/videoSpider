package com.spider.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseWeb {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final long  defaultSegmentSize=1024*1024*10;
}
