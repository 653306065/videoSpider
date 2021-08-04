package com.spider.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CommonJob {

    @Autowired
    RequestMappingHandlerMapping mapping;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Value("${limit.bucket}")
    private Integer bucket;

    @Value("${limit.rate}")
    private Integer rate;

    @Async("scheduleThreadPool")
    @Scheduled(fixedDelayString = "${limit.time}")
    public void limitJob() {
        Collection<HandlerMethod> handlerMethodList = mapping.getHandlerMethods().values();
        Map<String, String> map = handlerMethodList.stream().collect(Collectors.toMap(url -> url.getMethod().getDeclaringClass() + "#" + url.getMethod().getName(), url -> {
            int size = 0;
            String value = redisTemplate.opsForValue().get(String.valueOf(url.getMethod().hashCode()));
            if (Objects.nonNull(value)) {
                size = Integer.parseInt(value);
            }
            int nextSize = Math.min(size + rate, bucket);
            return nextSize + "";
        }, (o, n) -> o));
        redisTemplate.opsForValue().multiSet(map);
    }

}



