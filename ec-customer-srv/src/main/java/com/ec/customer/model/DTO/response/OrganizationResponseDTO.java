package com.ec.customer.model.DTO.response;

import com.ec.customer.model.entity.Organization;
import lombok.Data;

import java.util.Set;

@Data
public class OrganizationResponseDTO {
    private Long id;

    private String name;

    private String email;

    private Integer status;

    private String taxCode;

    private Set<OrganizationResponseDTO> children;
}
