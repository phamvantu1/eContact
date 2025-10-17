package com.ec.customer.controller;

import com.ec.customer.service.PermissionService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission API", description = "Quản lý thông tin quyền")
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "Danh sách phân quyền",description = "Lấy danh sách phân quyền có phân trang , tìm kiếm")
    @GetMapping("/get-all")
    public Response<?> getAllPermissions(@RequestParam(name = "page", defaultValue = "0") int page,
                                         @RequestParam(name ="size", defaultValue =  "10") int size,
                                         @RequestParam(name = "textSearch", required = false) String textSearch){
        return Response.success(permissionService.getAllPermissions(page, size, textSearch));
    }
}
