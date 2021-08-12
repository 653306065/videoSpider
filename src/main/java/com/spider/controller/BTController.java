package com.spider.controller;

import com.spider.entity.By114BT;
import com.spider.service.AvInfoService;
import com.spider.service.By114BTService;
import com.spider.service.VideoService;
import com.spider.vo.ResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @ApiOperation("根据关键字搜索")
    @GetMapping("/search/{keyword}")
    public ResponseVo<Object> search(@PathVariable String keyword) {
        return ResponseVo.succee(by114BTService.findByRegex("title", keyword).stream().map(bt -> new HashMap<String, Object>() {{
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
//        List<By114BT> btList = by114BTService.findAll();
//        btList.parallelStream().forEach(bt -> {
//            if (StringUtils.hasText(bt.getAvCode())) {
//                List<Video> videoList = videoService.findBykeyValue("avCode", bt.getAvCode());
//                if (CollectionUtil.isNotEmpty(videoList)) {
//                    logger.info("{},{}", bt.getTorrentPath(), bt.getAvCode());
//                    new File(bt.getTorrentPath()).delete();
//                    bt.getImagesPath().forEach(path -> new File(path).delete());
//                }
//            }
//            filterKeyList.stream().filter(key -> bt.getTitle().contains(key)).forEach(key -> {
//                logger.info("{},{}", bt.getTitle(), key);
//                new File(bt.getTorrentPath()).delete();
//                bt.getImagesPath().forEach(path -> new File(path).delete());
//            });
//        });

//        MongoCursor<Document> mongoCursor= by114BTService.getMongoCollection().find(new BasicDBObject()).iterator();
//        while (mongoCursor.hasNext()){
//            Document document= mongoCursor.next();
//            By114BT bt= new By114BT();
//            bt.setAvCode(document.getString("avCode"));
//            bt.setTorrentPath(document.getString("torrentPath"));
//            bt.setImagesPath(document.getList("imagesPath",String.class));
//            bt.setTitle(document.getString("title"));
//            if (StringUtils.hasText(bt.getAvCode())) {
//                List<Video> videoList = videoService.findBykeyValue("avCode", bt.getAvCode());
//                if (CollectionUtil.isNotEmpty(videoList)) {
//                    logger.info("{},{}", bt.getTorrentPath(), bt.getAvCode());
//                    new File(bt.getTorrentPath()).delete();
//                    bt.getImagesPath().forEach(path -> new File(path).delete());
//                }
//            }
//            filterKeyList.stream().filter(key -> bt.getTitle().contains(key)).forEach(key -> {
//                if(new File(bt.getTorrentPath()).exists()){
//                    new File(bt.getTorrentPath()).delete();
//                    logger.info("{},{}", bt.getTitle(), key);
//                    bt.getImagesPath().forEach(path -> new File(path).delete());
//                }
//            });
//        }
        return ResponseVo.succee();
    }
}
