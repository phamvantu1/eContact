package com.ec.auth.model.DTO.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @NotNull
    private String email;

    @NotNull
    private String password;
}
