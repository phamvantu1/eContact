package com.ec.contract.model.dto;

import lombok.Data;

@Data
public class OrganizationDTO {

    private Integer id;

    private String name;

    private String email;

    private Integer status;

    private String taxCode;

    private String code;

    private Integer parentId;

}
