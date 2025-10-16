package com.ec.auth.model.DTO.response;

import lombok.Data;

@Data
public class Response<T> {
    private String code;
    private String message;
    private T data;
}