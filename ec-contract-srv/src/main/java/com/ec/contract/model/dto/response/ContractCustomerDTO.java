package com.ec.contract.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContractCustomerDTO {

    private int customerId;

    private String customerName;

    private int totalContracts;
}
