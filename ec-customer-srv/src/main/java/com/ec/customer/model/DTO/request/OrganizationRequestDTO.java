package com.ec.customer.model.DTO.request;

import com.ec.customer.model.entity.Organization;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrganizationRequestDTO {

    @NotNull
    private String name;

    @NotNull
    private String email;

    @NotNull
    private String taxCode;

    @NotNull
    private String code;

    private Integer parentId;
}
