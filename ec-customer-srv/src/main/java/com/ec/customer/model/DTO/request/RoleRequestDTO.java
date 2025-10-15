package com.ec.customer.model.DTO.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RoleRequestDTO {
    private Long id;

    @NotNull
    private String name;

    List<Long> permissionIds;
}
