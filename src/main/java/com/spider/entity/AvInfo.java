package com.spider.entity;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Getter
@Setter
@Document(collection = "avInfo")
public class AvInfo {

    private String id;

    //名称
    private String name;

    //翻译名称
    private String translateName;

    //识别码
    private String code;

    //发布日期
    private Date publishDate;

    //时长
    private Integer minute;

    //制作商
    private String maker;

    //系列
    private String series;

    //标签
    private List<String> tags;

    //女优
    private List<String> avatars;

    //封面
    private String coverUrl;

    //缩略图地址
    private String thumbUrl;

    //缩略图
    private byte[] thumb;

    //封面文件
    private byte[] cover;

    //来源地址
    private String sourceUrl;

    //磁力链接
    private List<Magnet> magnetList;

    // 预览图
    private List<String>  previewImageUrlList;

    // 保存的预览图文件
    private List<byte[]> previewImageList;

    //本地是否有这个文件
    private boolean hasVideo;

    //保存的videoID
    private String videoId;

    //文件的保存路径
    private String videoSavePath;

    //创建日期
    private Date createDate;

    @Data
    @Setter
    @Getter
    public static class Magnet{

        //名称
        private String name;

        //磁力地址
        private String magnet;

        //文件大小
        private Double size;

        //文件大小的简写
        private String sizeStr;

        //分享日期
        private Date shareDate;

        //来源页
        private String sourceUrl;
    }
}
