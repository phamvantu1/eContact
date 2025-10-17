package com.ec.customer.controller;

import com.ec.customer.model.DTO.request.RoleRequestDTO;
import com.ec.customer.service.RoleService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Tag(name = "Role API", description = "Quản lý thông tin vai trò")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Tạo mới vai trò", description = "Thêm mới một vai trò vào hệ thống")
    @PostMapping("/create")
    public Response<?> create(@Valid @RequestBody RoleRequestDTO roleRequestDTO){
        return Response.success(roleService.createRole(roleRequestDTO)) ;
    }

    @Operation(summary = "Cập nhập vai trò", description = "Cập nhập vai trò theo ID")
    @PutMapping("/update/{roleId}")
    public Response<?> update(@Valid @RequestBody RoleRequestDTO roleRequestDTO,
                              @PathVariable("roleId") Integer roleId){
        return Response.success(roleService.updateRole(roleId, roleRequestDTO)) ;
    }

    @Operation(summary = "Xoá vai trò", description = "Xoá vai trò theo ID")
    @DeleteMapping("/delete/{roleId}")
    public Response<?> delete(@PathVariable("roleId") Integer roleId){
        return Response.success(roleService.deleteRole(roleId)) ;
    }

    @Operation(summary = "Danh sách vai trò", description = "Danh sach vai trò có phân trang và tìm kiếm")
    @GetMapping("/get-all")
    public Response<?> getAllRoles(@RequestParam(name = "textSearch", required = false, defaultValue = "") String textSearch,
                                   @RequestParam(name = "page" , required = false, defaultValue = "0") int page,
                                   @RequestParam(name = "size", required = false, defaultValue = "10") int size ){
        return Response.success(roleService.getAllRoles(page, size, textSearch));
    }

    @Operation(summary = "Thông tin chi tiết vai trò", description = "Thông tin chi tiết vai trò theo ID")
    @GetMapping("/{roleId}")
    public Response<?> getRoleById(@PathVariable("roleId") Integer roleId){
        return Response.success(roleService.getRoleById(roleId));
    }
}
