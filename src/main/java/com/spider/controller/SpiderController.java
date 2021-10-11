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
import java.util.concurrent.ForkJoinPool;

@Api(tags = "爬虫接口")
@RestController
@RequestMapping("/api/spider")
public class SpiderController extends BaseController {

    @Autowired
    private Javbangers javbangers;

    @Autowired
    private Javbus javbus;

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

    @Autowired
    private Netflav netflav;

    @Autowired
    private Eporner eporner;

    @Autowired
    private By114 by114;

    @Autowired
    private Hanime hanime;

    @Autowired
    private Pixiv pixiv;

    @Autowired
    private Wandusp wandusp;

    @Autowired
    private Javhihi javhihi;

    @Autowired
    private Hentaicomic hentaicomic;

    @ApiOperation("下载 hentaicomic 漫画")
    @GetMapping("/hentaicomic/start")
    public ResponseVo<Object> hentaicomic(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                      @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                      @RequestParam(name = "savePath", required = false) String savePath,
                                      @RequestParam(name = "tag", required = false) String tag) {
        hentaicomic.setEnableProxy(enableProxy);
        hentaicomic.setThread(thread);
        hentaicomic.setSavePath(savePath);
        threadPoolExecutor.execute(() -> hentaicomic.download(tag));
        return ResponseVo.succee();
    }

    @ApiOperation("下载 javhihi 无码视频")
    @GetMapping("/javhihi/uncensored")
    public ResponseVo<Object> javhihi(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                      @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                      @RequestParam(name = "savePath", required = false) String savePath) {
        javhihi.setEnableProxy(enableProxy);
        javhihi.setThread(thread);
        javhihi.setSavePath(savePath);
        threadPoolExecutor.execute(() -> javhihi.downloadVideo());
        return ResponseVo.succee();
    }

    @ApiOperation("下载 Wandusp 无码视频")
    @GetMapping("/wandusp/uncensored")
    public ResponseVo<Object> wandusp(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                      @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                      @RequestParam(name = "savePath", required = false) String savePath) {
        wandusp.setThread(thread);
        if (Objects.nonNull(savePath)) {
            wandusp.setSavePath(savePath);
        }
        wandusp.setEnableProxy(enableProxy);
        threadPoolExecutor.execute(() -> wandusp.downloadUncensored());
        return ResponseVo.succee();
    }

    @ApiOperation("下载 pixiv 排行图片")
    @GetMapping("/pixiv/rank")
    public ResponseVo<Object> pixivRank(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                        @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                        @RequestParam(name = "savePath", required = false) String savePath) {
        pixiv.setThread(thread);
        if (Objects.nonNull(savePath)) {
            pixiv.setSavePath(savePath);
        }
        pixiv.setEnableProxy(enableProxy);
        threadPoolExecutor.execute(() -> pixiv.downloadRankImage());
        return ResponseVo.succee();
    }


    @ApiOperation("下载 hanime nsfw类型图片")
    @GetMapping("/hanime/image/nsfw")
    public ResponseVo<Object> hanimeNsfwImage(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                              @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                              @RequestParam(name = "savePath", required = false) String savePath) {
        hanime.setThread(thread);
        if (Objects.nonNull(savePath)) {
            hanime.setSavePath(savePath);
        }
        hanime.setEnableProxy(enableProxy);
        threadPoolExecutor.execute(() -> hanime.download_nsfw_general_Image());
        return ResponseVo.succee();
    }

    @ApiOperation("下载 hanime irl_3d类型图片")
    @GetMapping("/hanime/image/irl_3d")
    public ResponseVo<Object> hanimeirl_3dImage(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                                @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                                @RequestParam(name = "savePath", required = false) String savePath) {
        hanime.setThread(thread);
        if (Objects.nonNull(savePath)) {
            hanime.setSavePath(savePath);
        }
        hanime.setEnableProxy(enableProxy);
        threadPoolExecutor.execute(() -> hanime.download_irl_3d_Image());
        return ResponseVo.succee();
    }

    @ApiOperation("开始by114 bt下载")
    @GetMapping("/start/by114")
    public ResponseVo<Object> startBy114(@RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                         @RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                         @RequestParam(name = "savePath", required = false) String savePath) {
        by114.setEnableProxy(enableProxy);
        by114.setThread(thread);
        by114.setSavePath(savePath);
        threadPoolExecutor.execute(() -> by114.downloadBt());
        return ResponseVo.succee();
    }


    @ApiOperation("开始eporner 4k下载")
    @GetMapping("/start/eporner/4k")
    public ResponseVo<Object> startEporner(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                           @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                           @RequestParam(name = "savePath", required = false) String savePath) {
        eporner.setThread(thread);
        eporner.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            eporner.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> eporner.download4K());
        return ResponseVo.succee();
    }

    @ApiOperation("开始javbangers下载")
    @GetMapping("/start/javbangers")
    public ResponseVo<Object> startJavbangers(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                              @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                              @RequestParam(name = "savePath", required = false) String savePath) {
        javbangers.setThread(thread);
        javbangers.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            javbangers.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> javbangers.downloadUncensored());
        return ResponseVo.succee();
    }

    @ApiOperation("开始保存javbus的avInfo")
    @GetMapping("/start/save/javbus/avInfo")
    public ResponseVo<Object> saveJavBusAvInfo(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                               @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                               @RequestParam(name = "savePath", required = false) String savePath) {
        javbus.setThread(thread);
        javbus.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            javbus.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> javbus.saveAvInfoByActressesAll());
        return ResponseVo.succee();
    }


    @ApiOperation("获取最新javbus的avInfo")
    @GetMapping("/start/save/javbus/new/avInfo")
    public ResponseVo<Object> saveJavBusNewAvInfo(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                                  @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                                  @RequestParam(name = "savePath", required = false) String savePath) {
        javbus.setThread(thread);
        javbus.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            javbus.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> javbus.saveNewAvInfo());
        return ResponseVo.succee();
    }

    @ApiOperation("更新javbus的磁力")
    @GetMapping("/start/update/javbus/magnet")
    public ResponseVo<Object> updateJavBusMagnet(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                                 @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                                 @RequestParam(name = "savePath", required = false) String savePath) {
        javbus.setThread(thread);
        javbus.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            javbus.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> javbus.updateAVMagnetList());
        return ResponseVo.succee();
    }

    @ApiOperation("保存javbus的女优")
    @GetMapping("/start/save/javbus/actresses")
    public ResponseVo<Object> saveJavBusActresses(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                                  @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                                  @RequestParam(name = "savePath", required = false) String savePath) {
        javbus.setThread(thread);
        javbus.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            javbus.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> javbus.saveAllUncensoredActressesInfo());
        return ResponseVo.succee();
    }

    @ApiOperation("保存xslist的女优信息")
    @GetMapping("/start/save/xslist/actresses")
    public ResponseVo<Object> saveXslist(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                         @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                         @RequestParam(name = "savePath", required = false) String savePath) {
        xslist.setEnableProxy(enableProxy);
        threadPoolExecutor.execute(() -> {
            ForkJoinPool forkJoinPool = new ForkJoinPool(thread);
            forkJoinPool.submit(() -> {
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
    public ResponseVo<Object> startJavrave(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                           @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                           @RequestParam(name = "savePath", required = false) String savePath) {
        javrave.setThread(thread);
        javrave.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            javrave.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> javrave.downloadUncensored());
        return ResponseVo.succee();
    }

    @ApiOperation("获取pornhub(compilation creampie)的视频")
    @GetMapping("/start/pornhub/compilation/creampie")
    public ResponseVo<Object> startPornhubCompilationCreampie(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                                              @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                                              @RequestParam(name = "savePath", required = false) String savePath) {
        pornhub.setThread(thread);
        pornhub.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            pornhub.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> pornhub.download_compilation_creampie());
        return ResponseVo.succee();
    }


    @ApiOperation("获取pornhub(compilation gangbang)的视频")
    @GetMapping("/start/pornhub/creampi/gangbang")
    public ResponseVo<Object> startPornhubCompilationGangbang(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                                              @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                                              @RequestParam(name = "savePath", required = false) String savePath) {
        pornhub.setThread(thread);
        pornhub.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            pornhub.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> pornhub.downloadCreampie_gangbang());
        return ResponseVo.succee();
    }

    @ApiOperation("获取pornhub(doublePenetration)的视频")
    @GetMapping("/start/pornhub/doublePenetration")
    public ResponseVo<Object> startPornhubDoublePenetration(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                                            @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                                            @RequestParam(name = "savePath", required = false) String savePath) {
        pornhub.setThread(thread);
        pornhub.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            pornhub.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> pornhub.downloadDoublePenetration());
        return ResponseVo.succee();
    }

    @ApiOperation("获取pornhub 搜索的视频")
    @GetMapping("/start/pornhub/search/{keyword}")
    public ResponseVo<Object> startPornhubCompilationGangbang(@PathVariable String keyword,
                                                              @RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                                              @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                                              @RequestParam(name = "savePath", required = false) String savePath) {
        pornhub.setThread(thread);
        pornhub.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            pornhub.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> pornhub.downloadSearch(keyword));
        return ResponseVo.succee();
    }


    @ApiOperation("获取图片的人脸信息")
    @PostMapping(value = "/face/info", headers = "content-type=multipart/form-data", consumes = "multipart/*")
    public ResponseVo<List<FaceInfo>> faceInfo(@ApiParam(value = "文件") MultipartFile file) {
        try {
            return ResponseVo.succee(FaceUtil.faceInfo(file.getBytes()));
        } catch (Exception e) {
            return ResponseVo.failure(-1, "获取文件失败");
        }
    }

    @ApiOperation("hqporner 4k视频下载")
    @GetMapping(value = "/start/hqporner/4k")
    public ResponseVo<List<FaceInfo>> hqporner4K(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                                 @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                                 @RequestParam(name = "savePath", required = false) String savePath) {
        hqporner.setThread(thread);
        hqporner.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            hqporner.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> hqporner.download4k());
        return ResponseVo.succee();
    }

    @ApiOperation("hqporner SexParties视频下载")
    @GetMapping(value = "/start/hqporner/SexParties")
    public ResponseVo<List<FaceInfo>> hqpornerSexParties(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                                         @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                                         @RequestParam(name = "savePath", required = false) String savePath) {
        hqporner.setThread(thread);
        hqporner.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            hqporner.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> hqporner.downloadSexParties());
        return ResponseVo.succee();
    }

    @ApiOperation("kissjav uncensored视频下载")
    @GetMapping(value = "/start/kissjav/uncensored")
    public ResponseVo<Object> kissjavUncensored(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                                @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                                @RequestParam(name = "savePath", required = false) String savePath) {
        kissjav.setThread(thread);
        kissjav.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            kissjav.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> kissjav.downloadJavUncensored());
        return ResponseVo.succee();
    }

    @ApiOperation("javhuge uncensored视频下载")
    @GetMapping(value = "/start/javhuge/uncensored")
    public ResponseVo<Object> javhugeUncensored(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                                @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                                @RequestParam(name = "savePath", required = false) String savePath) {
        javhuge.setThread(thread);
        javhuge.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            javhuge.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> javhuge.download("步兵无码"));
        return ResponseVo.succee();
    }

    @ApiOperation("javhuge 破解无码视频下载")
    @GetMapping(value = "/start/javhuge/break/uncensored")
    public ResponseVo<Object> javhugeBreakUncensored(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                                     @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                                     @RequestParam(name = "savePath", required = false) String savePath) {
        javhuge.setThread(thread);
        javhuge.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            javhuge.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> javhuge.download("无码破解"));
        return ResponseVo.succee();
    }

    @ApiOperation("javfull uncensored视频下载")
    @GetMapping(value = "/start/javfull/uncensored")
    public ResponseVo<Object> javfullUncensored(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                                @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                                @RequestParam(name = "savePath", required = false) String savePath) {
        javfull.setThread(thread);
        javfull.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            javhuge.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> javfull.downloadByCategory("uncensored"));
        return ResponseVo.succee();
    }

    @ApiOperation("netflav 无码视频下载")
    @GetMapping(value = "/start/netflav/uncensored")
    public ResponseVo<Object> netflavUncensored(@RequestParam(name = "thread", defaultValue = "5") Integer thread,
                                                @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                                @RequestParam(name = "savePath", required = false) String savePath,
                                                @RequestParam(name = "genre", required = false) String genre) {
        netflav.setThread(thread);
        netflav.setEnableProxy(enableProxy);
        if (Objects.nonNull(savePath)) {
            netflav.setSavePath(savePath);
        }
        threadPoolExecutor.execute(() -> netflav.downloadUncensoredVideo(genre));
        return ResponseVo.succee();
    }

}


