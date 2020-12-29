package com.spider.controller;

import com.spider.vo.ResponseVo;
import com.spider.web.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.ThreadPoolExecutor;

@Api(tags = "爬虫接口")
@RestController
@RequestMapping("/api/spider")
public class SpiderController {

    @Autowired
    private Javbangers javbangers;

    @Autowired
    private Javbus javbus;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private Shubao shubao;

    @Autowired
    private Javrave javrave;

    @Autowired
    private Pornhub pornhub;

    @ApiOperation("开始javbangers下载")
    @GetMapping("/start/javbangers")
    public ResponseVo<Object> startJavbangers(@RequestParam(name = "thread", defaultValue = "30") Integer thread) {
        javbangers.setThread(thread);
        threadPoolExecutor.execute(() -> javbangers.downloadUncensored());
        return ResponseVo.succee();
    }

    @ApiOperation("开始保存javbus的avInfo")
    @GetMapping("/start/save/javbus/avInfo")
    public ResponseVo<Object> saveJavBusAvInfo(@RequestParam(name = "thread", defaultValue = "30") Integer thread) {
        javbus.setThread(thread);
        threadPoolExecutor.execute(() -> javbus.saveAvInfoByActressesAll());
        return ResponseVo.succee();
    }

    @ApiOperation("更新javbus的avInfo" )
    @GetMapping("/start/update/javbus/avInfo")
    public ResponseVo<Object> updateJavBusAvInfo(@RequestParam(name = "thread", defaultValue = "30") Integer thread) {
        javbus.setThread(thread);
        threadPoolExecutor.execute(() -> javbus.saveAvInfoByActressesAll());
        return ResponseVo.succee();
    }

    @ApiOperation("获取书包网的书" )
    @GetMapping("/start/shubao")
    public ResponseVo<Object> startShubao() {
        threadPoolExecutor.execute(() -> shubao.getBookList());
        return ResponseVo.succee();
    }


    @ApiOperation("获取javrave的视频" )
    @GetMapping("/start/javrave")
    public ResponseVo<Object> startJavrave() {
        threadPoolExecutor.execute(() -> javrave.downloadUncensored());
        return ResponseVo.succee();
    }

    @ApiOperation("获取pornhub(compilation creampie)的视频" )
    @GetMapping("/start/pornhub/compilation/creampie")
    public ResponseVo<Object> startPornhub(@RequestParam(name = "thread", defaultValue = "30") Integer thread){
        pornhub.setThread(thread);
        threadPoolExecutor.execute(() -> pornhub.download_compilation_creampie());
        return ResponseVo.succee();
    }

}


