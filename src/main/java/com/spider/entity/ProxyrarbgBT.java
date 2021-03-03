package com.spider.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "proxyrarbgBT")
@Getter
@Setter
public class ProxyrarbgBT {

    private String id;

    private byte[] torrent;

    private String torrentName;

    private String torrentUrl;

    private String magnet;

    private byte[] poster;

    private String posterUrl;

    private String descriptionContent;

    private String descriptionHtml;

    private List<byte[]> descriptionImages;

    private List<String> descriptionImagesUrl;

    private String mediaInfo;

    private Double Rating;

    private String category;

    private Long size;

    private String sizeStr;

    private Date addDate;

    private String title;

    private String plot;

    private String tags;

    private String releaseName;
}
