package com.ec.contract.model.dto.request;

import com.ec.contract.model.entity.ContractRef;
import com.ec.contract.model.entity.Participant;
import jakarta.validation.constraints.NotNull;
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
public class ContractRequestDTO {

    @NotNull
    private String name;

    private String contractNo;

    @NotNull
    private LocalDateTime signTime;

    private String note;

    private Set<ContractRefRequestDTO> contractRefs;

    private Integer typeId; // loai hop dong

    private Boolean isTemplate; // la hop dong mau hay khong

    private Integer templateContractId;

    private LocalDateTime contractExpireTime; // ngay het han hop dong
}
