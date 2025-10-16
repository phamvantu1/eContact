package com.ec.customer.model.DTO.response;

import com.ec.customer.model.entity.Permission;
import lombok.Data;

import java.util.Set;

@Data
public class RoleResponseDTO {
    private Integer id;

    private String name;

    Set<Permission> permissions;
}
