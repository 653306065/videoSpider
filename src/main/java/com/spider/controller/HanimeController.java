package com.spider.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.spider.entity.HanimeImage;
import com.spider.service.HanimeImageService;
import com.spider.utils.FileUtils;
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
import java.util.List;
import java.util.Objects;
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
}
