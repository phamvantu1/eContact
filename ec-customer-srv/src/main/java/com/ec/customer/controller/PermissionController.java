package com.ec.customer.controller;

import com.ec.customer.service.PermissionService;
import com.ec.library.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/get-all")
    public Response<?> getAllPermissions(int page, int size) {
        return Response.success(permissionService.getAllPermissions(page, size));
    }
}
