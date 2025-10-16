package com.ec.customer.controller;

import com.ec.customer.model.DTO.request.RoleRequestDTO;
import com.ec.customer.service.RoleService;
import com.ec.library.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/create")
    public Response<?> create(@Valid @RequestBody RoleRequestDTO roleRequestDTO){
        return Response.success(roleService.createRole(roleRequestDTO)) ;
    }

    @PutMapping("/update/{roleId}")
    public Response<?> update(@Valid @RequestBody RoleRequestDTO roleRequestDTO,
                              @PathVariable("roleId") Integer roleId){
        return Response.success(roleService.updateRole(roleId, roleRequestDTO)) ;
    }
}
