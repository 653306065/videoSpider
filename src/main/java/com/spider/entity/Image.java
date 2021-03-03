package com.spider.entity;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "image")
@Getter
@Setter
public class Image {

    private String id;

    private String name;

    private long size;

    private String sizeStr;

    private String source;

    private String savePath;

    private String md5;

    private Date createDate;

    private Integer width;

    private Integer height;
}
