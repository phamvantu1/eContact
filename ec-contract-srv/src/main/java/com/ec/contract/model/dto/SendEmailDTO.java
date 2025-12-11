package com.ec.contract.model.dto;

import lombok.Data;

@Data
public class SendEmailDTO {

    private String subject;

    private String recipient;

    private String cc;

    private String content;

    private Integer status;

    private String code;

}
