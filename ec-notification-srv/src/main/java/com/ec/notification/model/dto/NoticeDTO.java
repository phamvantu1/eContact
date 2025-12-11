package com.ec.notification.model.dto;


import com.ec.library.entity.BaseEntity;
import lombok.Data;

@Data
public class NoticeDTO extends BaseEntity {

    private Integer id;

    private Integer contractId;

    private String noticeContent;

    private String noticeUrl;

    private String email;

}
