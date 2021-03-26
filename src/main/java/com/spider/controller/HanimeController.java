package com.spider.controller;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.spider.service.HanimeImageService;
import com.spider.vo.ResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            hanimeImageService.deleteKey(id,"image");
            logger.info("{},清空图片", id);
        }
        return ResponseVo.succee();
    }
}
