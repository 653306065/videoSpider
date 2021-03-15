package com.spider.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.spider.entity.By114BT;
import com.spider.entity.Video;
import com.spider.service.AvInfoService;
import com.spider.service.By114BTService;
import com.spider.service.VideoService;
import com.spider.vo.ResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;
import java.io.File;
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

    @Autowired
    VideoService videoService;

    @Value("${filterKey}")
    private List<String> filterKeyList;

    @ApiOperation("根据关键字搜索")
    @GetMapping("/search/{keyword}")
    public ResponseVo<Object> search(@PathVariable String keyword) {
        List<By114BT> list = new ArrayList<>();
        list.addAll(by114BTService.findByRegex("content", keyword));
        list.addAll(by114BTService.findByRegex("title", keyword));
        return ResponseVo.succee(list.stream().map(bt -> new HashMap<String, Object>() {{
            put("content", bt.getContent());
            put("title", bt.getTitle());
            put("id", bt.getId());
        }}).collect(Collectors.toList()));
    }

    @ApiOperation("保存AvCode")
    @GetMapping("/save/AaCode")
    public ResponseVo<Object> saveAvCode() {
        List<By114BT> btList = by114BTService.findAll();
        AvInfoService.codeTransformMap.entrySet().parallelStream().forEach(stringListEntry -> {
            stringListEntry.getValue().forEach(code -> btList.stream().filter(bt -> bt.getTitle().contains(code) || bt.getUrlName().contains(code)).forEach(bt -> {
                bt.setAvCode(stringListEntry.getKey());
                logger.info("{},avCode:{}", bt.getTitle(), stringListEntry.getValue());
                by114BTService.updateById(bt);
            }));
        });
        return ResponseVo.succee();
    }

    @ApiOperation("清除BT")
    @GetMapping("/clean/bt")
    public ResponseVo<Object> cleanBt() {
        List<By114BT> btList = by114BTService.findAll();
        btList.parallelStream().forEach(bt -> {
            if (StringUtils.hasText(bt.getAvCode())) {
                List<Video> videoList = videoService.findBykeyValue("avCode", bt.getAvCode());
                if (CollectionUtil.isNotEmpty(videoList)) {
                    logger.info("{},{}", bt.getTorrentPath(), bt.getAvCode());
                    new File(bt.getTorrentPath()).delete();
                    bt.getImagesPath().forEach(path -> new File(path).delete());
                }
            }
            filterKeyList.stream().filter(key -> bt.getTitle().contains(key)).forEach(key -> {
                logger.info("{},{}", bt.getTitle(), key);
                new File(bt.getTorrentPath()).delete();
                bt.getImagesPath().forEach(path -> new File(path).delete());
            });
        });
        return ResponseVo.succee();
    }

    @PostConstruct
    public void initFilterKey() {
        filterKeyList.addAll(filterKeyList.stream().map(String::toLowerCase).collect(Collectors.toList()));
        filterKeyList.addAll(filterKeyList.stream().map(String::toUpperCase).collect(Collectors.toList()));
    }
}
