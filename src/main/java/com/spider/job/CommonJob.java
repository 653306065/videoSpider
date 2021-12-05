package com.spider.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

//@Component
public class CommonJob {

    @Autowired
    RequestMappingHandlerMapping mapping;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Value("${limit.bucket}")
    private Integer bucket;

    @Value("${limit.rate}")
    private Integer rate;

    private List<String> keyList = new ArrayList<>();

    @Async("scheduleThreadPool")
    @Scheduled(fixedDelayString = "${limit.time}")
    public void limitJob() {
        List<String> valueList = redisTemplate.opsForValue().multiGet(keyList);
        AtomicInteger index = new AtomicInteger(0);
        Map<String, String> map = keyList.stream().collect(Collectors.toMap(key -> key, key -> {
            int size = 0;
            String value = valueList.get(index.get());
            if (Objects.nonNull(value)) {
                size = Integer.parseInt(value);
            }
            index.incrementAndGet();
            return String.valueOf(Math.min(size + rate, bucket));
        }));
        redisTemplate.opsForValue().multiSet(map);
    }

    @PostConstruct
    public void initKeyList() {
        Collection<HandlerMethod> handlerMethodList = mapping.getHandlerMethods().values();
        keyList.addAll(handlerMethodList.stream().map(handler -> handler.getMethod().getDeclaringClass() + "#" + handler.getMethod().getName()).distinct().collect(Collectors.toList()));
    }

}



