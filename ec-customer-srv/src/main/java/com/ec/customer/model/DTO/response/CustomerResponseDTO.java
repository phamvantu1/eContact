package com.ec.customer.model.DTO.response;

import lombok.Data;

import java.util.List;

@Data
public class CustomerResponseDTO {

    private Long id;

    private String name;

    private String email;

    private String password;

    private String phone;

    private String birthday;

    private String gender;

    private Integer status;

    private String taxCode;

    private Long organizationId;

    private List<RoleResponseDTO> roles;

}
