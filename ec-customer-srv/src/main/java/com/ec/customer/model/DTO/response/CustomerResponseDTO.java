package com.ec.customer.model.DTO.response;

import lombok.Data;

import java.util.List;

@Data
public class CustomerResponseDTO {
    private String name;

    private String email;

    private String password;

    private String phone;

    private String birthday;

    private String gender;

    private String status;

    private List<RoleResponseDTO> roles;

    private Integer organizationId;
}
