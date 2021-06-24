package com.spider.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@Document("hanimeImage")
public class HanimeImage {
    private Integer id;
    private String channelName;
    private String username;
    private String url;
    private String proxyUrl;
    private String extension;
    private Integer width;
    private Integer height;
    private Integer filesize;
    private Integer createdAtUnix;
    private Integer updatedAtUnix;
    private String discordUserId;
    private String userAvatarUrl;
    private String canonicalUrl;
    private String savePath;
    private String md5;
}
