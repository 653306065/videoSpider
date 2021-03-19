package com.spider.controller.mongo;

import com.spider.service.BaseService;
import com.spider.utils.SpringContentUtil;
import com.spider.vo.ResponseVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.ParameterizedType;
import java.util.List;

@CrossOrigin
public abstract class BaseMongodbController<service extends BaseService<entity>, entity> implements ApplicationRunner {

    protected Class<entity> clazz;

    protected Class<service> serviceClass;

    protected service service;

    @Override
    public void run(ApplicationArguments args) {
        clazz = (Class<entity>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        serviceClass = (Class<service>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        service = SpringContentUtil.getBean(serviceClass);
    }


    @ApiOperation("根据指定键和值查询数据")
    @GetMapping("/findByKeyValue")
    public ResponseVo<List<entity>> findByKeyValue(@RequestParam String key, @RequestParam String value) {
        return ResponseVo.succee(service.findBykeyValue(key, value));
    }

    @ApiOperation("根据指定键和值查询一条数据")
    @GetMapping("/findOneKeyValue")
    public ResponseVo<List<entity>> findOneByKeyValue(@RequestParam String key, @RequestParam String value) {
        return ResponseVo.succee(service.findOnekeyValue(key, value));
    }

    @ApiOperation("根据指定键和值查询数量")
    @GetMapping("/countByKeyValue")
    public ResponseVo<Long> countByKeyValue(@RequestParam String key, @RequestParam String value) {
        return ResponseVo.succee(service.count(key, value));
    }

    @ApiOperation("查询总数量")
    @GetMapping("/count")
    public ResponseVo<Long> count() {
        return ResponseVo.succee(service.count());
    }

    @ApiOperation("模糊查询")
    @GetMapping("/findByRegex")
    public ResponseVo<Long> count(@RequestParam String key, @RequestParam String value) {
        return ResponseVo.succee(service.findByRegex(key,value));
    }


    @ApiOperation("查询所有")
    @GetMapping("/findAll")
    public ResponseVo<Long> findAll() {
        return ResponseVo.succee(service.findAll());
    }

    @ApiOperation("模糊查询数量")
    @GetMapping("/findCountByRegex")
    public ResponseVo<Long> findAll(@RequestParam String key, @RequestParam String value) {
        return ResponseVo.succee(service.findCountByRegex(key, value));
    }

    @ApiOperation("模糊查询一条记录")
    @GetMapping("/findOneByRegex")
    public ResponseVo<Long> findOneByRegex(@RequestParam String key, @RequestParam String value) {
        return ResponseVo.succee(service.findOneByRegex(key, value));
    }


    @ApiOperation("根据id删除")
    @GetMapping("/removeById")
    public ResponseVo<Object> removeById(@RequestParam String id) {
        service.removeById(id);
        return ResponseVo.succee();
    }
}
