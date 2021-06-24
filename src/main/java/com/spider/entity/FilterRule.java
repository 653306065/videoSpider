package com.spider.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document("filterRule")
@Builder
public class FilterRule {

    private String id;

    private Rule type;

    private Boolean enable;

    private String rule;

    private LocalDateTime createTime;


    public enum Rule{
        /**
         * 正则表达式
         */
        RegExp,
        /**
         * 字符串
         */
        String
    }

}
