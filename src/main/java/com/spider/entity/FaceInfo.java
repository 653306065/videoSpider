package com.spider.entity;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class FaceInfo{

    private String gender;

    private Integer age;

    private Double maleScore;

    private Double femaleScore;

}