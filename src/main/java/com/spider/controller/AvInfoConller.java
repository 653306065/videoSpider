package com.spider.controller;

import cn.hutool.core.bean.BeanUtil;
import com.spider.entity.AvInfo;
import com.spider.service.es.EsAvInfoService;
import com.spider.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/avInfo")
public class AvInfoConller {

    @Autowired
    private EsAvInfoService esAvInfoService;

    @GetMapping("/search/{value}")
    public ResponseVo<List<AvInfo>> search (@PathVariable(value = "value", required = true) String value) {
        List<AvInfo> list = esAvInfoService.searchByValue(value).stream().map(esAvInfoSearchHit -> BeanUtil.toBean(esAvInfoSearchHit.getContent(), AvInfo.class)).collect(Collectors.toList());
        return ResponseVo.succee(list);
    }

}
