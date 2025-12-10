package com.ec.notification.model.DTO;

import lombok.Data;

@Data
public class EmailDTO {

    private Integer id;

    private String subject;

    private String recipient;

    private String cc;

    private String content;

    private Integer status;

}
