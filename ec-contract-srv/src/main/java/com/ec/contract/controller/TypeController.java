package com.ec.contract.controller;

import com.ec.contract.model.dto.TypeDTO;
import com.ec.contract.service.TypeService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/types")
@RequiredArgsConstructor
@Tag(name = "Type Controller API", description = "Quản lý thông tin loại hợp đồng")
public class TypeController {

    private final TypeService typeService;

    @PostMapping("")
    @Operation(summary = "Tạo loại hợp đồng mới", description = "Tạo một loại hợp đồng mới trong hệ thống.")
    public Response<?> createType(@Valid @RequestBody TypeDTO typeDTO) {
        return Response.success(typeService.createType(typeDTO));
    }

    @PutMapping("/{typeId}")
    @Operation(summary = "Cập nhật loại hợp đồng", description = "Cập nhật thông tin của một loại hợp đồng dựa trên ID loại hợp đồng.")
    public Response<?> updateType(@PathVariable(name = "typeId") Integer typeId,
                                  @Valid @RequestBody TypeDTO typeDTO) {
        return Response.success(typeService.updateType(typeId, typeDTO));
    }

    @DeleteMapping("/{typeId}")
    @Operation(summary = "Xóa loại hợp đồng", description = "Xóa một loại hợp đồng dựa trên ID loại hợp đồng.")
    public Response<?> deleteType(@PathVariable(name = "typeId") Integer typeId) {
        return Response.success(typeService.deleteType(typeId));
    }

    @GetMapping("/{typeId}")
    @Operation(summary = "Lấy thông tin loại hợp đồng theo ID", description = "Lấy thông tin chi tiết của một loại hợp đồng dựa trên ID loại hợp đồng.")
    public Response<?> getTypeById(@PathVariable(name = "typeId") Integer typeId) {
        return Response.success(typeService.getTypeById(typeId));
    }

    @GetMapping("")
    @Operation(summary = "Lấy tất cả loại hợp đồng", description = "Lấy tất cả loại hợp đồng trong hệ thống.")
    public Response<?> getAllTypes(@RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                   @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
                                   @RequestParam(name = "textSearch", required = false) String textSearch,
                                   @RequestParam(name = "organizationId", required = false ) Integer organizationId) {
        return Response.success(typeService.getAllTypes(page, size, textSearch, organizationId));
    }
}
