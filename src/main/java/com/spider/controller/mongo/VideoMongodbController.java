package com.spider.controller.mongo;

import com.spider.entity.Video;
import com.spider.service.VideoService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "video mongodb接口")
@RestController
@RequestMapping("/mongodb/video")
public class VideoMongodbController extends BaseMongodbController<VideoService, Video>{
}
