package com.spider.controller;

import com.spider.entity.By114BT;
import com.spider.service.By114BTService;
import com.spider.vo.ResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "bt接口")
@RestController
@RequestMapping("/api/bt")
public class BTController extends BaseController {

    @Autowired
    By114BTService by114BTService;

    @ApiOperation("根据关键字搜索")
    @GetMapping("/search/{keyword}")
    public ResponseVo<Object> search(@PathVariable String keyword) {
        List<By114BT> list=new ArrayList<>();
        list.addAll(by114BTService.findByRegex("content", keyword)) ;
        list.addAll(by114BTService.findByRegex("title", keyword)) ;
        return ResponseVo.succee(list.stream().map(bt-> new HashMap<String, Object>(){{
            put("content",bt.getContent());
            put("title",bt.getTitle());
            put("id",bt.getId());
        }}).collect(Collectors.toList()));
    }
}
