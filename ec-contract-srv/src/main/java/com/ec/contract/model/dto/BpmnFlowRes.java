package com.ec.contract.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class BpmnFlowRes {
    private BpmnRecipientDto createdBy;

    private LocalDateTime createdAt;

    private String reasonCancel;

    private LocalDateTime cancelDate;

    private List<BpmnRecipientDto> recipients;

    private int contractStatus;
}
