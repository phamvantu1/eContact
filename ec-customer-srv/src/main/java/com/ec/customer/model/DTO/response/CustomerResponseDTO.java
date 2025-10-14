package com.ec.customer.model.DTO.response;

import lombok.Data;

@Data
public class CustomerResponseDTO {
    private String name;

    private String email;

    private String password;

    private String phone;

    private String birthday;

    private String gender;

    private String status;

    private Integer roleId;

    private Integer organizationId;
}
