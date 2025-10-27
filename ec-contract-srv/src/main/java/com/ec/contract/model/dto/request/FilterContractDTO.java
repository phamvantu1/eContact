package com.ec.contract.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FilterContractDTO {

    private Integer status;

    private String textSearch;

    private String fromDate;

    private String toDate;

    @JsonProperty(defaultValue = "0")
    private Integer page;

    @JsonProperty(defaultValue = "10")
    private Integer size;

    private Integer organizationId;

}
