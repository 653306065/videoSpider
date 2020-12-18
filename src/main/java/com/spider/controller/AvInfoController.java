package com.spider.controller;

import cn.hutool.core.bean.BeanUtil;
import com.spider.entity.AvInfo;
import com.spider.service.es.EsAvInfoService;
import com.spider.vo.ResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "avInfo接口")
@RestController
@RequestMapping("/api/avInfo")
public class AvInfoController extends BaseController {

    @Autowired
    private EsAvInfoService esAvInfoService;

    @ApiOperation("根据关键字搜索")
    @GetMapping("/search/{value}")
    public ResponseVo<List<AvInfo>> search(@PathVariable(value = "value", required = true) String value) {
        List<AvInfo> list = esAvInfoService.searchByValue(value).stream().map(esAvInfoSearchHit -> BeanUtil.toBean(esAvInfoSearchHit.getContent(), AvInfo.class)).collect(Collectors.toList());
        return ResponseVo.succee(list);
    }

}
