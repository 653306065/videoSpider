package com.spider.controller;

import com.spider.entity.FaceInfo;
import com.spider.utils.FaceUtil;
import com.spider.vo.ResponseVo;
import com.spider.web.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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

    @Autowired
    private Hqporner hqporner;

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


    @ApiOperation("获取图片的人脸信息" )
    @PostMapping(value="/face/info",headers = "content-type=multipart/form-data",consumes = "multipart/*")
    public ResponseVo<List<FaceInfo>> faceInfo(@ApiParam(value = "文件",required = true) MultipartFile file){
        try {
            return ResponseVo.succee(FaceUtil.faceInfo(file.getBytes())) ;
        }catch (Exception e){
            return ResponseVo.failure(-1,"获取文件失败");
        }
    }

    @ApiOperation("hqporner 4k视频下载")
    @GetMapping(value="/start/hqporner/4k")
    public ResponseVo<List<FaceInfo>> hqporner4K(@RequestParam(name = "thread", defaultValue = "30") Integer thread){
        hqporner.setThread(thread);
        threadPoolExecutor.execute(() -> hqporner.download4k());
        return ResponseVo.succee();
    }

    @ApiOperation("hqporner SexParties视频下载")
    @GetMapping(value="/start/hqporner/SexParties")
    public ResponseVo<List<FaceInfo>> hqpornerSexParties(@RequestParam(name = "thread", defaultValue = "30") Integer thread){
        hqporner.setThread(thread);
        threadPoolExecutor.execute(() -> hqporner.downloadSexParties());
        return ResponseVo.succee();
    }

}


