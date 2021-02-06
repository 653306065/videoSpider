package com.spider.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import com.spider.entity.ActressesInfo;
import com.spider.entity.FaceInfo;
import com.spider.service.ActressesInfoService;
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
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

@Api(tags = "爬虫接口")
@RestController
@RequestMapping("/api/spider")
public class SpiderController extends BaseController {

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

    @Autowired
    private Xslist xslist;

    @Autowired
    private Kissjav kissjav;

    @Autowired
    private ActressesInfoService actressesInfoService;

    @Autowired
    private Javhuge javhuge;

    @Autowired
    private Javfull javfull;

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


    @ApiOperation("获取最新javbus的avInfo")
    @GetMapping("/start/save/javbus/new/avInfo")
    public ResponseVo<Object> saveJavBusNewAvInfo(@RequestParam(name = "thread", defaultValue = "30") Integer thread) {
        javbus.setThread(thread);
        threadPoolExecutor.execute(() -> javbus.saveNewAvInfo());
        return ResponseVo.succee();
    }

    @ApiOperation("更新javbus的磁力")
    @GetMapping("/start/update/javbus/magnet")
    public ResponseVo<Object> updateJavBusMagnet(@RequestParam(name = "thread", defaultValue = "30") Integer thread) {
        javbus.setThread(thread);
        threadPoolExecutor.execute(() -> javbus.updateAVMagnetList());
        return ResponseVo.succee();
    }

    @ApiOperation("保存javbus的女优")
    @GetMapping("/start/save/javbus/actresses")
    public ResponseVo<Object> saveJavBusActresses(@RequestParam(name = "thread", defaultValue = "30") Integer thread) {
        javbus.setThread(thread);
        threadPoolExecutor.execute(() -> javbus.saveAllUncensoredActressesInfo());
        return ResponseVo.succee();
    }

    @ApiOperation("保存xslist的女优信息")
    @GetMapping("/start/save/xslist/actresses")
    public ResponseVo<Object> saveXslist(@RequestParam(name = "thread", defaultValue = "30") Integer thread) {
        threadPoolExecutor.execute(() -> {
            actressesInfoService.findAll().stream().parallel().forEach(actressesInfo -> {
                List<String> urlList = xslist.getSearchList(actressesInfo.getName());
                if (CollectionUtil.isNotEmpty(urlList)) {
                    ActressesInfo info = xslist.getInfo(urlList.get(0));
                    if (Objects.nonNull(info) && actressesInfo.getName().trim().equals(info.getName().trim())) {
                        CopyOptions copyOptions = new CopyOptions();
                        copyOptions.setIgnoreNullValue(true);
                        BeanUtil.copyProperties(info, actressesInfo, copyOptions);
                        actressesInfoService.updateById(actressesInfo);
                        logger.info("{},信息获取完成", actressesInfo.getName());
                    }
                }
            });
            logger.info("获取xslist信息完成");
        });
        return ResponseVo.succee();
    }


    @ApiOperation("获取书包网的书")
    @GetMapping("/start/shubao")
    public ResponseVo<Object> startShubao() {
        threadPoolExecutor.execute(() -> shubao.getBookList());
        return ResponseVo.succee();
    }


    @ApiOperation("获取javrave的视频")
    @GetMapping("/start/javrave")
    public ResponseVo<Object> startJavrave() {
        threadPoolExecutor.execute(() -> javrave.downloadUncensored());
        return ResponseVo.succee();
    }

    @ApiOperation("获取pornhub(compilation creampie)的视频")
    @GetMapping("/start/pornhub/compilation/creampie")
    public ResponseVo<Object> startPornhubCompilationCreampie(@RequestParam(name = "thread", defaultValue = "30") Integer thread) {
        pornhub.setThread(thread);
        threadPoolExecutor.execute(() -> pornhub.download_compilation_creampie());
        return ResponseVo.succee();
    }

    @ApiOperation("获取pornhub(doublePenetration)的视频")
    @GetMapping("/start/pornhub/doublePenetration")
    public ResponseVo<Object> startPornhubDoublePenetration(@RequestParam(name = "thread", defaultValue = "30") Integer thread) {
        pornhub.setThread(thread);
        threadPoolExecutor.execute(() -> pornhub.downloadDoublePenetration());
        return ResponseVo.succee();
    }


    @ApiOperation("获取图片的人脸信息")
    @PostMapping(value = "/face/info", headers = "content-type=multipart/form-data", consumes = "multipart/*")
    public ResponseVo<List<FaceInfo>> faceInfo(@ApiParam(value = "文件", required = true) MultipartFile file) {
        try {
            return ResponseVo.succee(FaceUtil.faceInfo(file.getBytes()));
        } catch (Exception e) {
            return ResponseVo.failure(-1, "获取文件失败");
        }
    }

    @ApiOperation("hqporner 4k视频下载")
    @GetMapping(value = "/start/hqporner/4k")
    public ResponseVo<List<FaceInfo>> hqporner4K(@RequestParam(name = "thread", defaultValue = "30") Integer thread) {
        hqporner.setThread(thread);
        threadPoolExecutor.execute(() -> hqporner.download4k());
        return ResponseVo.succee();
    }

    @ApiOperation("hqporner SexParties视频下载")
    @GetMapping(value = "/start/hqporner/SexParties")
    public ResponseVo<List<FaceInfo>> hqpornerSexParties(@RequestParam(name = "thread", defaultValue = "30") Integer thread) {
        hqporner.setThread(thread);
        threadPoolExecutor.execute(() -> hqporner.downloadSexParties());
        return ResponseVo.succee();
    }

    @ApiOperation("kissjav uncensored视频下载")
    @GetMapping(value = "/start/kissjav/uncensored")
    public ResponseVo<Object> kissjavUncensored(@RequestParam(name = "thread", defaultValue = "30") Integer thread) {
        kissjav.setThread(thread);
        threadPoolExecutor.execute(() -> kissjav.downloadJavUncensored());
        return ResponseVo.succee();
    }

    @ApiOperation("javhuge uncensored视频下载")
    @GetMapping(value = "/start/javhuge/uncensored")
    public ResponseVo<Object> javhugeUncensored(@RequestParam(name = "thread", defaultValue = "30") Integer thread) {
        kissjav.setThread(thread);
        threadPoolExecutor.execute(() -> javhuge.download("步兵无码"));
        return ResponseVo.succee();
    }

    @ApiOperation("javhuge 破解无码视频下载")
    @GetMapping(value = "/start/javhuge/break/uncensored")
    public ResponseVo<Object> javhugeBreakUncensored(@RequestParam(name = "thread", defaultValue = "30") Integer thread) {
        kissjav.setThread(thread);
        threadPoolExecutor.execute(() -> javhuge.download("无码破解"));
        return ResponseVo.succee();
    }

    @ApiOperation("javfull uncensored视频下载")
    @GetMapping(value = "/start/javfull/uncensored")
    public ResponseVo<Object> test(@RequestParam(name = "thread", defaultValue = "30") Integer thread) {
        kissjav.setThread(thread);
        threadPoolExecutor.execute(() -> javfull.downloadByCategory("uncensored"));
        return ResponseVo.succee();
    }

}


