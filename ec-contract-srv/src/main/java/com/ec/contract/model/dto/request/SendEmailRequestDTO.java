package com.ec.contract.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequestDTO {

    private String subject; // chủ đề email

    private Integer contractId;

    private Integer recipientId;

    private String code;

    private String actionButton;

    private String titleEmail;

    private String url;

}
