package com.ec.customer.model.DTO.request;

import com.ec.customer.model.entity.Organization;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrganizationRequestDTO {
    private Long id;

    @NotNull
    private String name;

    private String email;

    @NotNull
    private String taxCode;

    private Long parentId;
}
