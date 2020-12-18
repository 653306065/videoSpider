package com.spider.controller;

import com.spider.vo.ResponseVo;
import com.spider.web.Javbangers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("/api/spider")
public class SpiderController {

    @Autowired
    private Javbangers javbangers;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @RequestMapping("/start/javbangers")
    public ResponseVo<Object> startJavbangers(@RequestParam(name = "thread", defaultValue = "30") Integer thread) {
        if (Objects.nonNull(thread)) {
            javbangers.setThread(thread);
        }
        threadPoolExecutor.execute(() -> javbangers.downloadUncensored());
        return ResponseVo.succee();
    }
}


