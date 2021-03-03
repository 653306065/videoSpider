package com.spider.entity;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "by114BT")
@Getter
@Setter
public class By114BT {

    private String id;

    private String urlName;

    private String url;

    private String type;

    private String title;

    private String videoName;

    private String subtitle;

    private String mosaic;

    private String magnet;

    private String BtDeadline;

    private String content;

    private String contentHtml;

    private String videoFormat;

    private String actorName;

    private String videoSize;

    private List<byte[]> images;

    private List<String> imagesPath;

    private List<String> imagesUrl;

    private byte[] torrent;

    private String torrentPath;

    private String torrentUrl;

    private String torrentName;

    private String videoPublishDate;

    private String contentPublishDate;

    private Date createData;

    private String pageHtml;
}
