package com.ec.notification.model.dto;

import lombok.Data;

@Data
public class SendEmailDTO {

    private String subject; // chủ đề email

    private String recipient; // email người nhận

    private String cc;

    private String content; // nội dung email

    private Integer status;

    private String code;

    private String contractName;

    private String contractNo;

    private String nameRecipient; // tên người xử lý hợp đồng

    private String nameSender; // tên người gửi hợp đồng

    private String note;

    private String url;

}
