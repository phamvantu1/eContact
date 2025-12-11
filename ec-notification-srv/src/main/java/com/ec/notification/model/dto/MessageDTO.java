package com.ec.notification.model.dto;

import lombok.Data;

@Data
public class MessageDTO {

    private Integer id;

    private String name;

    private String mailTemplate;

    private String url;

    private String noticeTemplate;
}
