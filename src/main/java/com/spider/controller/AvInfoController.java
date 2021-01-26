package com.spider.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.spider.entity.AvInfo;
import com.spider.entity.FaceInfo;
import com.spider.service.ActressesInfoService;
import com.spider.service.es.EsAvInfoService;
import com.spider.utils.FaceUtil;
import com.spider.vo.ResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private ThreadPoolExecutor threadPoolExecutor;

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
            actressesInfoService.findAll().stream().parallel().forEach(actressesInfo -> {
                List<byte[]> phtotList = new ArrayList<>();
                if (Objects.nonNull(actressesInfo.getJavbusPhoto())) {
                    phtotList.add(actressesInfo.getJavbusPhoto());
                }
                if (CollectionUtil.isNotEmpty(actressesInfo.getXsListImageList())) {
                    phtotList.addAll(actressesInfo.getXsListImageList());
                }
                List<FaceInfo> faceInfoList = phtotList.stream().parallel().flatMap(bytes -> Objects.requireNonNull(FaceUtil.faceInfo(bytes)).stream()).filter(Objects::nonNull).collect(Collectors.toList());
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
}
