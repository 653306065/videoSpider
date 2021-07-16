package com.spider.conf;

import com.spider.interceptor.LimitInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class WebConfig implements WebMvcConfigurer, ApplicationContextAware, ApplicationRunner {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Value("${limit.bucket}")
    private Integer bucket;

    @Value("${limit.rate}")
    private Integer rate;

    @Value("${limit.time}")
    private Integer time;


    ApplicationContext applicationContext;

    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LimitInterceptor(redisTemplate));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        List<String> urlList = mapping.getHandlerMethods().keySet().stream().flatMap(key -> key.getPatternsCondition().getPatterns().stream()).distinct().collect(Collectors.toList());
        new Thread(() -> {
            while (true) {
                Map<String, String> urlMap = urlList.stream().collect(Collectors.toMap(url -> url, url -> {
                    int size = Integer.valueOf(redisTemplate.opsForValue().get(url));
                    int nextSize = Math.min(size + rate, bucket);
                    return nextSize + "";
                }));
                redisTemplate.opsForValue().multiSet(urlMap);
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
