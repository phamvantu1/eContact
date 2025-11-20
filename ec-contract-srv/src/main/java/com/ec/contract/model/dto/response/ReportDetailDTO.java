package com.ec.contract.model.dto.response;

import com.ec.contract.model.dto.ParticipantDTO;
import com.ec.contract.model.entity.Customer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class ReportDetailDTO {

    private int id;

    private String name;

    private String typeName;

    private String contractNo;

    private List<ContractRefResponseDTO> refs;

    private LocalDateTime createdAt;

    private LocalDateTime contractExpireTime;

    private Integer status;

    private LocalDateTime completeDate;

    private LocalDateTime cancelDate;

    private LocalDateTime updatedAt;

    private String customer;

    private List<ParticipantDTO> participants;

    private String organizationCreatedName;

}
