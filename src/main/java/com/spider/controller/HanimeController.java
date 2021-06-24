package com.spider.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.spider.entity.HanimeImage;
import com.spider.service.HanimeImageService;
import com.spider.utils.FileUtils;
import com.spider.utils.OKHttpUtils;
import com.spider.vo.ResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Api(tags = "Hanime接口")
@RequestMapping("/api/hanime")
@RestController
public class HanimeController extends BaseController {

    @Autowired
    private HanimeImageService hanimeImageService;

    @ApiOperation("清空图片")
    @GetMapping("/clean/image")
    public ResponseVo<Object> cleanImage() {
        MongoCursor<Document> mongoCursor = hanimeImageService.getMongoCollection().find(new BasicDBObject()).iterator();
        while (mongoCursor.hasNext()) {
            Document document = mongoCursor.next();
            Integer id = document.getInteger("_id");
            hanimeImageService.deleteKey(id, "image");
            logger.info("{},清空图片", id);
        }
        return ResponseVo.succee();
    }

    @ApiOperation("清空指定低于分辨率图片")
    @GetMapping("/clean/low/dpi/image/")
    public ResponseVo<Integer> cleanImage(@RequestParam(value = "width", defaultValue = "250") Integer width, @RequestParam(value = "height", defaultValue = "250") Integer height) {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        hanimeImageService.findAll().stream().parallel().filter(image ->
                Objects.nonNull(image.getWidth()) && Objects.nonNull(image.getHeight())).filter(image ->
                image.getWidth() * image.getHeight() < width * height).forEach(image -> {
                    File file = new File(image.getSavePath());
                    if (file.exists() && file.delete()) {
                        atomicInteger.incrementAndGet();
                        logger.info("{},清空图片,宽:{},高:{},{}", image.getId(), image.getWidth(), image.getHeight(), image.getSavePath());
                    }
                }
        );
        return ResponseVo.succee(atomicInteger.get());
    }


    @ApiOperation("保存文件夹数据")
    @GetMapping("/save/image")
    public ResponseVo<Object> saveImage(@RequestParam String imagePath) {
        List<HanimeImage> hanimeImageList = hanimeImageService.findAll();
        List<File> fileList = new ArrayList<>();
        FileUtils.getPathFileList(imagePath, fileList);
        hanimeImageList.stream().parallel().forEach(hanimeImage -> {
            List<File> list = fileList.stream().filter(file -> file.getName().equals(hanimeImage.getId() + "." + hanimeImage.getExtension())).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(list)) {
                hanimeImage.setSavePath(list.get(0).getAbsolutePath());
                hanimeImageService.updateById(hanimeImage);
                logger.info("{},保存路径", hanimeImage.getId());
            }
        });
        return ResponseVo.succee();
    }


    @ApiOperation("下载数据库的的图片")
    @GetMapping("/download/db/image")
    public ResponseVo<Object> downloadDbImage(@RequestParam(defaultValue = "30") Integer thread,
                                              @RequestParam(name = "enableProxy", defaultValue = "false") Boolean enableProxy,
                                              @RequestParam String savePath) {
        List<HanimeImage> hanimeImageList = hanimeImageService.findAll();
        ForkJoinPool forkJoinPool = new ForkJoinPool(thread);
        forkJoinPool.execute(() -> {
            hanimeImageList.stream().parallel().filter(hanimeImage -> !new File(hanimeImage.getSavePath()).exists()).forEach(hanimeImage -> {
                byte[] bytes = OKHttpUtils.getBytes(hanimeImage.getUrl(), enableProxy);
                if (Objects.nonNull(bytes)) {
                    String path = savePath + hanimeImage.getChannelName() + File.separator + hanimeImage.getId() + "." + hanimeImage.getExtension();
                    if (FileUtils.byteToFile(bytes, path)) {
                        hanimeImage.setSavePath(path);
                        hanimeImageService.updateById(hanimeImage);
                        logger.info("id:{},url:{},下载完成", hanimeImage.getId(), hanimeImage.getUrl());
                    }
                }
            });
        });
        return ResponseVo.succee();
    }


    @ApiOperation("拆分文件到文件夹")
    @GetMapping("/splitFile")
    public ResponseVo<Object> splitFile(@RequestParam String path, @RequestParam Integer size, @RequestParam String channelName) {
        List<HanimeImage> hanimeImageList = hanimeImageService.findBykeyValue("channelName", channelName);
        hanimeImageList = hanimeImageList.stream().filter(hanimeImage -> new File(hanimeImage.getSavePath()).exists()).sorted(Comparator.comparing(HanimeImage::getId)).collect(Collectors.toList());
        int pageCount = (int) Math.round(hanimeImageList.size() * 1.0 / size) + 1;
        for (int i = 0; i < pageCount; i++) {
            File splitPath = new File(path + File.separator + i + File.separator);
            splitPath.mkdirs();
            List<HanimeImage> list = hanimeImageList.stream().skip((long) i * size).limit(size).collect(Collectors.toList());
            list.forEach(hanimeImage -> {
                File image = new File(hanimeImage.getSavePath());
                File newFile = new File(splitPath.getAbsolutePath() + File.separator + image.getName());
                image.renameTo(newFile);
                hanimeImage.setSavePath(newFile.getAbsolutePath());
                hanimeImageService.updateById(hanimeImage);
                logger.info("{}->{}", image.getAbsolutePath(), newFile.getAbsolutePath());
            });
        }
        return ResponseVo.succee();
    }
}
