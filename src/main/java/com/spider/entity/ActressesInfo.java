package com.spider.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Getter
@Setter
@Document(collection = "actressesInfo")
public class ActressesInfo {

    private String id;

    private String name;

    private String javbusUrl;

    private byte[] javbusPhoto;

    private String javbusPhotoUrl;

    private Boolean uncensored;

    private Boolean censored;

    private Date createDate;
}
