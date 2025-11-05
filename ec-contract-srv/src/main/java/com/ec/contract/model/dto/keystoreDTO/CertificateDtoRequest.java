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
    @JsonProperty("cert_id")
    private Integer certificate_id;

    private String isTimestamp = "false";
//    @NotBlank
    @JsonProperty("image_base64")
    private String imageBase64;

    private FieldDto field;

    // width height phuc vu cho ky nhieu
    private Float width;

    private Float height;

    private Integer type;

}
