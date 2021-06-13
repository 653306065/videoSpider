package com.spider.controller;

import com.spider.entity.FilterRule;
import com.spider.service.FilterRuleServcie;
import com.spider.vo.ResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Api(tags = "过滤规则接口")
@RequestMapping("/api/filterRule")
@RestController
public class FilterRuleController {

    @Autowired
    private FilterRuleServcie filterRuleServcie;

    @ApiOperation("添加文本过滤")
    @PostMapping("/string/list")
    public ResponseVo<Object> addStringRule(@RequestBody List<String> list) {
        list.forEach(value -> {
            FilterRule filterRule = FilterRule.builder().type(FilterRule.Rule.String).rule(value).enable(true).createTime(LocalDateTime.now()).build();
            filterRuleServcie.insert(filterRule);
        });
        return ResponseVo.succee();
    }

    @ApiOperation("添加正则表达式过滤")
    @PostMapping("/regExp/list")
    public ResponseVo<Object> addRegExpRule(@RequestBody List<String> list) {
        list.forEach(value -> {
            FilterRule filterRule = FilterRule.builder().type(FilterRule.Rule.RegExp).rule(value).enable(true).createTime(LocalDateTime.now()).build();
            filterRuleServcie.insert(filterRule);
        });
        return ResponseVo.succee();
    }

}
