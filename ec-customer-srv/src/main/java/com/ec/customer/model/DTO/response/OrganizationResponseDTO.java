package com.ec.customer.model.DTO.response;

import com.ec.customer.model.entity.Organization;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class OrganizationResponseDTO {
    private Integer id;

    private String name;

    private String email;

    private Integer status;

    private String taxCode;

    private String code;

    private Integer parentId;

}
