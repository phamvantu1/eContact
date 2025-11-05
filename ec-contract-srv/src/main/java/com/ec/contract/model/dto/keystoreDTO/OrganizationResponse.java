package com.ec.contract.model.dto.keystoreDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrganizationResponse {
    private Integer id;

    private String name;
    private int status;
    @JsonProperty("parent_id")
    private Integer parentId;
}
