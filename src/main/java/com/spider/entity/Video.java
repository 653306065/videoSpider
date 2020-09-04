package com.spider.entity;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import ws.schild.jave.MultimediaInfo;

@Document(collection = "video")
@Setter
@Getter
@Data
public class Video {

    private String id;

    private String name;

    private Long size;

    private String sizeStr;

    private String savePath;

    private String md5;

    private Date createDate;

    private MultimediaInfo multimediaInfo;

    private String videoUrl;

    private String sourceUrl;

    private Date pubDate;

    private List<String> starNames;

    private String introduction;

    private List<String> categories;

    private List<String> tags;

    private String quality;

    private String production;

    private String format;

    private String avCode;
}
