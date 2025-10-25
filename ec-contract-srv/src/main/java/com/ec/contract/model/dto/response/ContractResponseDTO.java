package com.ec.contract.model.dto.response;

import com.ec.contract.model.entity.ContractRef;
import com.ec.contract.model.entity.Participant;
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

    private Set<ContractRefResponseDTO> contractRefs;

    private Set<ParticipantResponseDTO> participants;

    private Integer typeId; // loai hop dong

    private Integer customerId; // id nguoi tao

    private Boolean isTemplate; // la hop dong mau hay khong

    private Integer status;

    private Integer organizationId; // to chuc

    private String reasonReject; // ly do tu choi hop dong

    private Integer templateContractId;

    private LocalDateTime contractExpireTime; // ngay het han hop dong

}
