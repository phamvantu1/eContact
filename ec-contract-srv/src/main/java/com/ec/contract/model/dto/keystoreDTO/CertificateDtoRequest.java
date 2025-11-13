package com.ec.contract.model.dto.keystoreDTO;

import com.ec.contract.model.dto.FieldDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CertificateDtoRequest {
    @JsonIgnore
    private String email;

    @JsonIgnore
    private String phone;

    @NotNull
    private Integer certId;

    private String isTimestamp = "false";
//    @NotBlank

    private String imageBase64;

    private FieldDto field;

    // width height phuc vu cho ky nhieu
    private Float width;

    private Float height;

    private Integer type;

}
