package com.spider.controller;

import cn.hutool.core.bean.BeanUtil;
import com.spider.constant.Constant;
import com.spider.entity.AvInfo;
import com.spider.entity.Video;
import com.spider.service.AvInfoService;
import com.spider.service.VideoService;
import com.spider.service.es.EsVideoService;
import com.spider.utils.BaiduTranslateUtil;
import com.spider.utils.FFmpegUtil;
import com.spider.utils.FileUtils;
import com.spider.utils.MD5Util;
import com.spider.vo.ResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import ws.schild.jave.MultimediaInfo;

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
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

    @Autowired
    private AvInfoService avInfoService;

    @ApiOperation("根据关键字搜索视频")
    @GetMapping("/search/{value}")
    public ResponseVo<List<Video>> search(@PathVariable(value = "value") String value) {
        List<Video> list = esVideoService.searchByValue(value).stream().map(esAvInfoSearchHit -> BeanUtil.toBean(esAvInfoSearchHit.getContent(), Video.class)).collect(Collectors.toList());
        return ResponseVo.succee(list);
    }

    @ApiOperation("清空低于指定分辨率的视频")
    @GetMapping("/clean/resolutionRatio/video")
    public ResponseVo<Object> cleanVideo(@RequestParam(required = false, defaultValue = "640") Integer height, @RequestParam(required = false, defaultValue = "640") Integer width, @RequestParam(defaultValue = "false") Boolean isDelete) {
        AtomicLong size = new AtomicLong(0);
        CopyOnWriteArrayList<String> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        videoService.findAll().stream().filter(video -> !video.getSavePath().contains("里番")).filter(video -> new File(video.getSavePath()).exists()).filter(video -> Objects.nonNull(video.getMultimediaInfo())).forEach(video -> {
            if (height * width > video.getMultimediaInfo().getVideo().getSize().getHeight() * video.getMultimediaInfo().getVideo().getSize().getWidth()) {
                logger.info(video.getSavePath());
                if(isDelete){
                    new File(video.getSavePath()).delete();
                }
                size.addAndGet(video.getSize());
                copyOnWriteArrayList.add(video.getSavePath());
            }
        });
        return ResponseVo.succee(new HashMap<String, Object>() {{
            put("fileSize", size.get() / 1024.0 / 1024 / 1024);
            put("list", copyOnWriteArrayList);
            put("listSize", copyOnWriteArrayList.size());
        }});
    }

    @ApiOperation("清空低于指定分钟数的视频")
    @GetMapping("/clean/time/video")
    public ResponseVo<Object> cleanTimeVideo(@RequestParam(required = false, defaultValue = "10") Integer minute,@RequestParam(defaultValue = "false") Boolean isDelete) {
        AtomicLong size = new AtomicLong(0);
        CopyOnWriteArrayList<String> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        videoService.findAll().stream().filter(video -> !video.getSavePath().contains("里番")).filter(video -> new File(video.getSavePath()).exists()).filter(video -> Objects.nonNull(video.getMultimediaInfo())).forEach(video -> {
            if (video.getMultimediaInfo().getDuration() < 1000L * 60 * minute) {
                logger.info(video.getSavePath());
                if(isDelete){
                    new File(video.getSavePath()).delete();
                }
                size.addAndGet(video.getSize());
                copyOnWriteArrayList.add(video.getSavePath());
            }
        });
        return ResponseVo.succee(new HashMap<String, Object>() {{
            put("fileSize", size.get() / 1024.0 / 1024 / 1024);
            put("list", copyOnWriteArrayList);
            put("listSize", copyOnWriteArrayList.size());
        }});
    }

    @ApiOperation("同步视频code")
    @GetMapping("/sync/code")
    public ResponseVo<Object> syncCode() {
        List<AvInfo> avInfoList = avInfoService.findAll();
        Set<String> codeSet = avInfoList.stream().map(AvInfo::getCode).collect(Collectors.toSet());
        List<Video> videoList = videoService.findAll();
        videoList.stream().filter(video -> Objects.isNull(video.getAvCode())).parallel().forEach(video -> {
            for (String code : codeSet) {
                if (video.getName().contains(code)) {
                    video.setAvCode(code);
                    videoService.updateById(video);
                    break;
                }
            }
        });
        return ResponseVo.succee();
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
        File file = new File(screenshotPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!file.isDirectory()) {
            return ResponseVo.failure(-1, "截图路径不是文件夹");
        }
        for (int i = 0; i < count; i++) {
            long index0 = multimediaInfo.getDuration() / (10 - i) / 1000;
            FFmpegUtil.videoSnapshot(videoPath, screenshotPath, String.valueOf(i), index0, 1);
        }
        List<File> fileList = new ArrayList<>();
        FileUtils.getPathFileList(screenshotPath, fileList);
        return ResponseVo.succee(fileList.stream().map(File::getAbsolutePath).collect(Collectors.toList()));
    }


    @ApiOperation("保存目标地址的视频信息")
    @GetMapping(value = "/save/info")
    public ResponseVo<Object> savePathVideoInfo(@RequestParam("path") String path) {
        List<File> fileList = new ArrayList<>();
        FileUtils.getPathFileList(path, fileList);
        List<File> videoList = fileList.stream().filter(file -> {
            String name = file.getName();
            for (String format : Constant.videoFormat) {
                if (name.endsWith(format)) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
        videoList.forEach(file -> {
            if (Objects.isNull(videoService.findByName(file.getName()))) {
                String md5 = MD5Util.md5(file);
                Video findVideo = videoService.findByMd5(md5);
                if (Objects.isNull(findVideo)) {
                    Video video = new Video();
                    video.setName(file.getName());
                    video.setSavePath(file.getAbsolutePath());
                    video.setMd5(md5);
                    video.setSize(file.length());
                    video.setCreateDate(new Date());
                    video.setMultimediaInfo(FFmpegUtil.getVideoInfo(file));
                    video.setSizeStr(file.length() / 1024.0 / 1024 + "MB");
                    List<String> keyList = FileUtils.getSearchKeyList(video.getName());
                    for (String key : keyList) {
                        AvInfo avInfo = avInfoService.findOnekeyValue("code", key);
                        if (Objects.nonNull(avInfo)) {
                            video.setAvCode(avInfo.getCode());
                        }
                    }
                    if (Objects.nonNull(video.getAvCode())) {
                        Video avVideo = videoService.findOnekeyValue("avCode", video.getAvCode());
                        if (Objects.nonNull(avVideo)) {
                            logger.info("{}已存在,{}存在的,目标:{}", video.getAvCode(), avVideo.getSavePath(), file.getAbsolutePath());
                            file.delete();
                        }
                    }
                    videoService.insert(video);
                    logger.info("{},保存完成", video.getSavePath());
                } else {
                    if (!findVideo.getSavePath().equals(file.getAbsolutePath())) {
                        logger.info("{}存在的,目标:{}", findVideo.getSavePath(), file.getAbsolutePath());
                        file.delete();
                    }
                }
            }
        });
        return ResponseVo.succee();
    }

    @ApiOperation("清空MD5一样的视频")
    @GetMapping("/clean/same/md5/video")
    public ResponseVo<Object> cleanSameMd5Video() {
        List<Video> videoList = videoService.findAll();
        Map<String, List<Video>> videoMap = videoList.stream().filter(video -> Objects.nonNull(video.getMd5())).collect(Collectors.groupingBy(Video::getMd5));
        for (Map.Entry<String, List<Video>> entry : videoMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                List<Video> sameList = entry.getValue().stream().skip(1).limit(entry.getValue().size() - 1).collect(Collectors.toList());
                sameList.forEach(video -> {
                    new File(video.getSavePath()).delete();
                    logger.info("{},删除成功", video.getSavePath());
                });
            }
        }
        logger.info("清空视频完成");
        return ResponseVo.succee();
    }

    @ApiOperation("清空code一样的视频")
    @GetMapping("/clean/same/code/video")
    public ResponseVo<Object> cleanSameCodeVideo() {
        List<Video> videoList = videoService.findAll();
        Map<String, List<Video>> videoMap = videoList.stream().filter(video -> Objects.nonNull(video.getAvCode())).collect(Collectors.groupingBy(Video::getAvCode));
        for (Map.Entry<String, List<Video>> entry : videoMap.entrySet()) {
            List<Video> codeList = entry.getValue().stream().filter(video -> new File(video.getSavePath()).exists()).collect(Collectors.toList());
            if (codeList.size() > 1) {
                codeList.forEach(video -> {
                    new File(video.getSavePath()).delete();
                    logger.info("{},{},删除成功", video.getAvCode(), video.getSavePath());
                });
                logger.info("-----------------");
            }
        }
        logger.info("清空视频完成");
        return ResponseVo.succee();
    }

    @ApiOperation("获取下载视频的总大小")
    @GetMapping("/total/size")
    public ResponseVo<Object> getVideoTotalSize() {
        List<Video> videoList = videoService.findAll();
        long totalSize = videoList.stream().filter(video -> Objects.nonNull(video.getSize())).mapToLong(Video::getSize).sum();
        return ResponseVo.succee(totalSize / 1024.0 / 1024 / 1024);
    }

    @ApiOperation("翻译名称")
    @GetMapping("/translate/name")
    public ResponseVo<Object> translateName() {
        threadPoolExecutor.execute(() -> {
            ForkJoinPool forkJoinPool = new ForkJoinPool(10);
            List<Video> videoList = videoService.findAll();
            forkJoinPool.submit(() -> videoList.parallelStream().forEach(video -> {
                String translateName = BaiduTranslateUtil.translate(video.getName().replace("mp4", ""), "auto", "zh");
                if (Objects.nonNull(translateName)) {
                    video.setTranslateName(translateName);
                    logger.info("{}->{}", video.getName(), translateName);
                    videoService.updateById(video);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    logger.info("{},翻译失败", video.getName());
                }
            }));
        });
        return ResponseVo.succee();
    }


    @ApiOperation("视频评分")
    @GetMapping("/score")
    public ResponseVo<Object> videoScore(@RequestParam(defaultValue = "65.0") double threshold,@RequestParam(defaultValue = "false") Boolean isDelete){
        videoService.findByexists("avgFaceScore",false).stream().filter(video -> new File(video.getSavePath()).exists()).forEach(video -> {
            videoService.videoScore(video.getId(),20,threshold,isDelete);
        });
        return ResponseVo.succee();
    }
}
