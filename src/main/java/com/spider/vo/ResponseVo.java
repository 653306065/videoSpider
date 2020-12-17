package com.spider.vo;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class ResponseVo<T> {

    private Integer code;

    private T data;

    private String msg;

    public static <Object>ResponseVo succee() {
        return ResponseVo.builder().code(0).msg("succee").build();
    }

    public static <T>ResponseVo succee(T t) {
        return ResponseVo.builder().code(0).data(t).msg("succee").build();
    }

    public static ResponseVo failure(Integer code, String msg) {
        return ResponseVo.builder().code(code).msg(msg).build();
    }
}
