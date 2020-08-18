package com.spider.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Javbus extends BaseWeb{

    @Value("${javbus.home}")
    private String home;

    @Value("${javbus.enableProxy}")
    private Boolean enableProxy;

    @Value("${javbus.thread}")
    private Integer thread;

    @Value("${javbus.actressesListTemplate}")
    private String actressesListTemplate;

    @Value("${javbus.actressesDetailsTemplate}")
    private String actressesDetailsTemplate;

    @Value("${javbus.avTemplate}")
    private String avTemplate;


}
