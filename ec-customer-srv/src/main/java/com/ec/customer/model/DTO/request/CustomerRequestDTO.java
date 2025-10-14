package com.ec.customer.model.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerRequestDTO {
    @NotBlank(message = "Tên không được để trống")
    private String name;

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

    private String password;

    @Pattern(
            regexp = "^(0|\\+84)(\\d{9})$",
            message = "Số điện thoại không hợp lệ (phải có 10 số, bắt đầu bằng 0 hoặc +84)"
    )
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    private String birthday;

    private String gender;

    @NotNull
    private Long organizationId;

    @NotNull
    private String status;

    @NotNull
    private Long roleId;
}
