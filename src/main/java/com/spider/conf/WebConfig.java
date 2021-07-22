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
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
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
        registry.addInterceptor(new LimitInterceptor(redisTemplate)).addPathPatterns("/api/**");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Collection<HandlerMethod> handlerMethodList = mapping.getHandlerMethods().values();
        new Thread(() -> {
            while (true) {
                Map<String, String> map = handlerMethodList.stream().collect(Collectors.toMap(url -> url.getMethod().getDeclaringClass()+"#"+url.getMethod().getName(), url -> {
                    int size = 0;
                    String value = redisTemplate.opsForValue().get(String.valueOf(url.getMethod().hashCode()));
                    if (Objects.nonNull(value)) {
                        size = Integer.parseInt(value);
                    }
                    int nextSize = Math.min(size + rate, bucket);
                    return nextSize + "";
                }, (o, n) -> o));
                redisTemplate.opsForValue().multiSet(map);
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }
}
