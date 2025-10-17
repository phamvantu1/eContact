package com.ec.auth.controller;

import com.ec.auth.model.DTO.request.CustomerRequestDTO;
import com.ec.auth.model.DTO.request.LoginRequestDTO;
import com.ec.auth.service.AuthService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth API", description = "Quản lý xác thực")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Đăng nhập")
    @PostMapping("/login")
    public Response<?> login(@Valid @RequestBody LoginRequestDTO request) {

        return Response.success(authService.login(request));
    }

    @Operation(summary = "Đăng ký", description = "Fe sử dụng cái này để đăng ký tài khoản")
    @PostMapping("/register")
    public Response<?> register(@Valid @RequestBody CustomerRequestDTO customerRequestDTO){
        return Response.success(authService.register(customerRequestDTO));
    }

    @Operation(summary = "Đăng xuất", description = "Đăng xuất và đưa token vào blacklist")
    @PostMapping("/logout")
    public Response<?> logout(HttpServletRequest request){
        return Response.success(authService.logout(request));
    }


}
