package com.spider.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.spider.entity.ActressesInfo;
import com.spider.entity.AvInfo;
import com.spider.entity.FaceInfo;
import com.spider.service.ActressesInfoService;
import com.spider.service.AvInfoService;
import com.spider.service.es.EsAvInfoService;
import com.spider.utils.BaiduTranslateUtil;
import com.spider.utils.FaceUtil;
import com.spider.vo.ResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Api(tags = "avInfo接口")
@RestController
@RequestMapping("/api/avInfo")
public class AvInfoController extends BaseController {

    @Autowired
    private EsAvInfoService esAvInfoService;

    @Autowired
    private ActressesInfoService actressesInfoService;

    @Autowired
    private AvInfoService avInfoService;

    @ApiOperation("根据关键字搜索")
    @GetMapping("/search/{value}")
    public ResponseVo<List<AvInfo>> search(@PathVariable(value = "value", required = true) String value) {
        List<AvInfo> list = esAvInfoService.searchByValue(value).stream().map(esAvInfoSearchHit -> BeanUtil.toBean(esAvInfoSearchHit.getContent(), AvInfo.class)).collect(Collectors.toList());
        return ResponseVo.succee(list);
    }


    @ApiOperation("获取av女优的脸部信息")
    @GetMapping("/save/actresses/faceInfo")
    public ResponseVo<List<AvInfo>> actressesFaceInfo() {
        threadPoolExecutor.execute(() -> {
            List<ActressesInfo> actressesInfoList = actressesInfoService.findAll();
            actressesInfoList.stream().sequential().forEach(actressesInfo -> {
                List<byte[]> phtotList = new ArrayList<>();
                if (Objects.nonNull(actressesInfo.getJavbusPhoto())) {
                    phtotList.add(actressesInfo.getJavbusPhoto());
                }
                if (CollectionUtil.isNotEmpty(actressesInfo.getXsListImageList())) {
                    phtotList.addAll(actressesInfo.getXsListImageList());
                }
                List<FaceInfo> faceInfoList = phtotList.stream().sequential().filter(Objects::nonNull).flatMap(bytes -> {
                    List<FaceInfo> list = FaceUtil.faceInfo(bytes);
                    if (CollectionUtil.isEmpty(list)) {
                        return null;
                    }
                    try {
                        Thread.sleep(350);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return list.stream();
                }).filter(Objects::nonNull).collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(faceInfoList)) {
                    double femaleScore = faceInfoList.stream().mapToDouble(FaceInfo::getFemaleScore).average().getAsDouble();
                    double maleScore = faceInfoList.stream().mapToDouble(FaceInfo::getMaleScore).average().getAsDouble();
                    double age = faceInfoList.stream().mapToDouble(FaceInfo::getAge).average().getAsDouble();
                    faceInfoList.get(0).setAge((int) age);
                    faceInfoList.get(0).setMaleScore(maleScore);
                    faceInfoList.get(0).setFemaleScore(femaleScore);
                    actressesInfo.setFaceInfo(faceInfoList.get(0));
                    actressesInfoService.updateById(actressesInfo);
                    logger.info("{},女性评分:{},男性评分:{},年龄:{}", actressesInfo.getName(), femaleScore, maleScore, age);
                }
            });
        });
        return ResponseVo.succee();
    }

    @ApiOperation("获取av的磁力链接")
    @GetMapping("/code/{code}/magnet/list")
    public ResponseVo<Object> findAVCodeMagnetList(@PathVariable("code") String code) {
        List<AvInfo> avInfoList = avInfoService.findByRegex("code", code);
        List<AvInfo.Magnet> magnetList = avInfoList.stream().filter(avInfo -> CollectionUtil.isNotEmpty(avInfo.getMagnetList())).map(avInfo -> avInfo.getMagnetList().stream().max(Comparator.comparing(AvInfo.Magnet::getSize)).get()).collect(Collectors.toList());
        return ResponseVo.succee(magnetList);
    }

    @ApiOperation("翻译名称")
    @GetMapping("/translate/name")
    public ResponseVo<Object> findAVCodeMagnetList() {
        threadPoolExecutor.execute(() -> {
            ForkJoinPool forkJoinPool = new ForkJoinPool(10);
            forkJoinPool.submit(() -> avInfoService.findByexists("translateName", false).parallelStream().forEach(avInfo -> {
                try {
                    String translateName = BaiduTranslateUtil.translate(avInfo.getName(), "auto", "zh");
                    if (Objects.nonNull(translateName)) {
                        logger.info("{}->{}", avInfo.getName(), translateName);
                        avInfo.setTranslateName(translateName);
                        avInfoService.updateById(avInfo);
                    } else {
                        logger.info("{},翻译失败", avInfo.getName());
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));
        });
        return ResponseVo.succee();
    }
}
