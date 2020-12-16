package com.spider.exception;

import com.spider.vo.ResponseVo;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value =Exception.class)
    public ResponseVo<Object> exceptionHandler(Exception e) {
        return ResponseVo.failure(-1,e.getMessage());
    }
}
