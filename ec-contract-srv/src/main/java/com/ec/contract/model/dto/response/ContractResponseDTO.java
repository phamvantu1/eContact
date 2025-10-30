package com.ec.contract.model.dto.response;

import com.ec.contract.model.dto.ParticipantDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponseDTO {

    private Integer id;

    private String name;

    private String contractNo;

    private LocalDateTime signTime;

    private String note;

    private Integer typeId; // loai hop dong

    private Integer customerId; // id nguoi tao

    private Boolean isTemplate; // la hop dong mau hay khong

    private Integer templateContractId;

    private Integer status;

    private Integer organizationId; // to chuc

    private String reasonReject; // ly do tu choi hop dong

    private LocalDateTime contractExpireTime; // ngay het han hop dong

    private Set<ContractRefResponseDTO> contractRefs;

    private Set<ParticipantDTO> participants;

    private Integer createdBy;

    private Integer updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
