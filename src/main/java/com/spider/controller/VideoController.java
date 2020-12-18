package com.spider.controller;

import cn.hutool.core.bean.BeanUtil;
import com.spider.entity.Video;
import com.spider.service.VideoService;
import com.spider.service.es.EsVideoService;
import com.spider.vo.ResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Api(tags = "视频接口")
@RestController
@RequestMapping("/api/video")
public class VideoController extends BaseController {

    @Autowired
    private EsVideoService esVideoService;

    @Autowired
    private VideoService videoService;

    @ApiOperation("根据关键字搜索视频")
    @GetMapping("/search/{value}")
    public ResponseVo<List<Video>> search(@PathVariable(value = "value", required = true) String value) {
        List<Video> list = esVideoService.searchByValue(value).stream().map(esAvInfoSearchHit -> BeanUtil.toBean(esAvInfoSearchHit.getContent(), Video.class)).collect(Collectors.toList());
        return ResponseVo.succee(list);
    }

    @ApiOperation("清空低于指定分辨率的视频")
    @GetMapping("/clean/video")
    public ResponseVo<Object> cleanVideo(@RequestParam(required = false, defaultValue = "640") Integer height, @RequestParam(required = false, defaultValue = "640") Integer width) {
        AtomicLong size = new AtomicLong(0);
        videoService.findAll().stream().filter(video -> new File(video.getSavePath()).exists()).filter(video -> Objects.nonNull(video.getMultimediaInfo())).forEach(video -> {
            if (height * width > video.getMultimediaInfo().getVideo().getSize().getHeight() * video.getMultimediaInfo().getVideo().getSize().getWidth()) {
                logger.info(video.getSavePath());
                new File(video.getSavePath()).delete();
                size.addAndGet(video.getSize());
            }
        });
        return ResponseVo.succee(size.get() / 1024.0 / 1024 / 1024);
    }
}
