package com.ec.contract.model.dto.signatureDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignatureDtoRequest {
    private Integer signType;
    private String signBy;
    private String taxCodeOrIdentification;
    private String iconPosition;
    private String base64Image;
    private Date dateActionSign;
    private Integer typeImageSignature;
    private Boolean hiddenPhone;
}
