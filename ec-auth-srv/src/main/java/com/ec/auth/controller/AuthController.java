package com.ec.auth.controller;

import com.ec.auth.model.DTO.request.LoginRequestDTO;
import com.ec.auth.service.AuthService;
import com.ec.library.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Response<?> login(@Valid @RequestBody LoginRequestDTO request) {

        return Response.success(authService.login(request));
    }


}
