package com.ec.contract.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CertRequest {
    @NotBlank
    private String userName;
    @NotBlank
    private String password;
}
