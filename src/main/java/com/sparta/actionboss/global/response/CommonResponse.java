package com.sparta.actionboss.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {
    private String msg;
    private T data;

    public CommonResponse(String msg, T data){
        this.msg = msg;
        this.data = data;
    }
    public CommonResponse(String msg){
        this.msg = msg;
    }
    public CommonResponse(T data){
        this.data = data;
    }
}