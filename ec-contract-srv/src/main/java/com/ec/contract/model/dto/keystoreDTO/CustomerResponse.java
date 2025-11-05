package com.ec.contract.model.dto.keystoreDTO;


import lombok.Data;

@Data
public class CustomerResponse {
    private int id;
    private String roleName;
    private int organizationId;
}
