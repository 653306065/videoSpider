package com.spider.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Getter
@Setter
@Document(collection = "actressesInfo")
public class ActressesInfo {

    private String id;

    //名称
    private String name;

    //英文名
    private String enName;

    //javbus的介绍页
    private String javbusUrl;

    //javbus的头像
    private byte[] javbusPhoto;

    //javbus的头像链接
    private String javbusPhotoUrl;

    //是否有无码片
    private Boolean uncensored;

    //是否有有码片
    private Boolean censored;

    private Date createDate;

    //javbus图片识别结果
    private FaceInfo faceInfo;

    //身高
    private Integer height;

    //杯罩
    private String cup;

    //血型
    private String bloodType;

    //出道日期
    private Date debutDate;

    //生日
    private Date birthday;

    //三围
    private String BWH;

    //星座
    private String constellation;

    //别名
    private List<String> alis;

    //国籍
    private String nationality;

    //xsList网站的图片
    private List<byte[]> xsListImageList;

    //xsList网站的图片url
    private List<String> xsListImageUrlList;

}
