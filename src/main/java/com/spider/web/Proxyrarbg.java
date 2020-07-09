package com.spider.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Proxyrarbg {

    @Value("${proxyrarbg.home}")
    private String home;

    @Value("${proxyrarbg.template}")
    private String template;
}
