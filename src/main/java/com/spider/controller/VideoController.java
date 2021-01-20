package com.spider.controller;

import cn.hutool.core.bean.BeanUtil;
import com.spider.entity.Video;
import com.spider.service.VideoService;
import com.spider.service.es.EsVideoService;
import com.spider.utils.FFmpegUtil;
import com.spider.utils.FileUtils;
import com.spider.vo.ResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ws.schild.jave.MultimediaInfo;
import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
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
        CopyOnWriteArrayList<String> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        videoService.findAll().stream().filter(video -> new File(video.getSavePath()).exists()).filter(video -> Objects.nonNull(video.getMultimediaInfo())).forEach(video -> {
            if (height * width > video.getMultimediaInfo().getVideo().getSize().getHeight() * video.getMultimediaInfo().getVideo().getSize().getWidth()) {
                logger.info(video.getSavePath());
                //new File(video.getSavePath()).delete();
                size.addAndGet(video.getSize());
                copyOnWriteArrayList.add(video.getSavePath());
            }
        });
        Map<String, Object> map = new HashMap<>();
        map.put("fileSize", size.get() / 1024.0 / 1024 / 1024);
        map.put("list", copyOnWriteArrayList);
        map.put("listSize", copyOnWriteArrayList.size());
        return ResponseVo.succee(map);
    }

    @ApiOperation("获取视频截图")
    @GetMapping(value = "/screenshot")
    public ResponseVo<List<String>> getVideoScreenshot(@RequestParam("videoPath") String videoPath,
                                                       @RequestParam("screenshotPath") String screenshotPath,
                                                       @RequestParam("count") Integer count) {
        MultimediaInfo multimediaInfo = FFmpegUtil.getVideoInfo(new File(videoPath));
        if (Objects.isNull(multimediaInfo)) {
            return ResponseVo.failure(-1, "文件非视频");
        }
        File file=new File(screenshotPath);
        if(!file.exists()){
            file.mkdirs();
        }
        if(!file.isDirectory()){
            return ResponseVo.failure(-1, "截图路径不是文件夹");
        }
        for (int i = 0; i < count; i++) {
            long index0 = (long) (multimediaInfo.getDuration() / (10 - i)) / 1000;
            FFmpegUtil.videoSnapshot(videoPath, screenshotPath, String.valueOf(i), index0, 1);
        }
        List<File> fileList = new ArrayList<>();
        FileUtils.getPathFileList(screenshotPath, fileList);
        return ResponseVo.succee(fileList.stream().map(File::getAbsolutePath).collect(Collectors.toList()));
    }
}
