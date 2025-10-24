package com.ec.contract.model.dto.response;

import com.ec.contract.model.entity.Contract;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractRefResponseDTO {

    private Integer id;

    private Contract contract;

    private Integer refId;
}
