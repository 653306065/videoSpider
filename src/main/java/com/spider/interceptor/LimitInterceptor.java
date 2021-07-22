package com.spider.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Objects;

public class LimitInterceptor implements HandlerInterceptor {

    Logger logger = LoggerFactory.getLogger(LimitInterceptor.class);

    private RedisTemplate<String, String> redisTemplate;

    public LimitInterceptor(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url = request.getRequestURI();
        logger.info("{} url:{}", request.getMethod(), request.getRequestURL());
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        String size = redisTemplate.opsForValue().get(String.valueOf(method.hashCode()));
        if (Objects.nonNull(size) && Integer.parseInt(size) < 0) {
            throw new Exception("接口限流");
        }
        redisTemplate.opsForValue().decrement(url);
        return true;
    }
}
