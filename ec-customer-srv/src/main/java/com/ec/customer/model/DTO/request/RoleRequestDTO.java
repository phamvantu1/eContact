package com.ec.customer.model.DTO.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RoleRequestDTO {

    @NotNull
    private String name;

    List<Integer> permissionIds;
}
