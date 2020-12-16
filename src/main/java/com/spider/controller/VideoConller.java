package com.spider.controller;

import cn.hutool.core.bean.BeanUtil;
import com.spider.entity.Video;
import com.spider.service.es.EsVideoService;
import com.spider.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/video")
public class VideoConller {

    @Autowired
    private EsVideoService esVideoService;

    @GetMapping("/search/{value}")
    public ResponseVo<List<Video>> search(@PathVariable(value = "value", required = true) String value) {
        List<Video> list = esVideoService.searchByValue(value).stream().map(esAvInfoSearchHit -> BeanUtil.toBean(esAvInfoSearchHit.getContent(), Video.class)).collect(Collectors.toList());
        return ResponseVo.succee(list);
    }
}
