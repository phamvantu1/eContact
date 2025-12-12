package com.ec.contract.model.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendEmailRequestDTO {

    private String subject; // chủ đề email

    private Integer contractId;

    private Integer recipientId;

    private String code;

    private String actionButton;

    private String titleEmail;

    private String url;

}
