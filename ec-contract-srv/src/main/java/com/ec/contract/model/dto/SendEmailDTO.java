package com.ec.contract.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendEmailDTO {

    private String subject; // chủ đề email

    private String recipientEmail; // email người nhận

    private String cc;

    private String content; // nội dung email

    private Integer status;

    private String code;

    private String contractName;

    private String contractNo;

    private String recipientName; // tên người xử lý hợp đồng

    private String senderName; // tên người gửi hợp đồng

    private String note;

    private String url;

    private String actionButton;

    private String titleEmail;

}
