package com.ec.auth.controller;

import com.ec.auth.model.DTO.request.CustomerRequestDTO;
import com.ec.auth.model.DTO.request.LoginRequestDTO;
import com.ec.auth.service.AuthService;
import com.ec.library.response.Response;
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
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Response<?> login(@Valid @RequestBody LoginRequestDTO request) {

        return Response.success(authService.login(request));
    }

    @PostMapping("/register")
    public Response<?> register(@Valid @RequestBody CustomerRequestDTO customerRequestDTO){
        return Response.success(authService.register(customerRequestDTO));
    }

    @PostMapping("/logout")
    public Response<?> logout(HttpServletRequest request){
        return Response.success(authService.logout(request));
    }


}
