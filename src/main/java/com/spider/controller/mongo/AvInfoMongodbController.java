package com.spider.controller.mongo;

import com.spider.entity.AvInfo;
import com.spider.service.AvInfoService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "avInfo mongodb接口")
@RestController
@RequestMapping("/mongodb/avInfo")
public class AvInfoMongodbController extends BaseMongodbController<AvInfoService, AvInfo> {
}
