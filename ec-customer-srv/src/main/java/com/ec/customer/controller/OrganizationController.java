package com.ec.customer.controller;

import com.ec.customer.model.DTO.request.OrganizationRequestDTO;
import com.ec.customer.model.DTO.response.OrganizationResponseDTO;
import com.ec.customer.service.OrganizationService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization API ", description = "Quản lý thông tin tổ chức")
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "Tạo mới tổ chức ",description = "Thêm mới một tổ chức vào hệ thống")
    @PostMapping("/create")
    public Response<?> createOrganization(@Valid @RequestBody OrganizationRequestDTO organizationRequestDTO){
        return Response.success(organizationService.createOrganization(organizationRequestDTO));
    }

    @Operation(summary = "Xóa 1 tổ chức ",description = "Xóa tổ chức theo ID")
    @DeleteMapping("/delete/{organizationId}")
    public Response<?> deleteOrganization(@PathVariable("organizationId") Integer organizationId){
        return Response.success(organizationService.deleteOrganization(organizationId));
    }

    @Operation(summary = "Cập nhập tổ chức ",description = "Cập nhật dữ liệu tổ chức theo ID")
    @PutMapping("/update/{organizationId}")
    public Response<?> updateOrganization(@Valid @RequestBody OrganizationRequestDTO organizationRequestDTO,
                                         @PathVariable("organizationId") Integer organizationId){
        return Response.success(organizationService.updateOrganization(organizationId, organizationRequestDTO));
    }

    @Operation(summary = "Lấy danh sách tất cả tổ chức ",description = "Lấy danh sách tổ chức có phân trang và tìm kiếm")
    @GetMapping("/get-all")
    public Response<?> getAllOrganization(@RequestParam(name = "textSearch", required = false, defaultValue = "") String textSearch,
                                          @RequestParam(name = "page" , required = false, defaultValue = "0") int page,
                                          @RequestParam(name = "size", required = false, defaultValue = "10") int size){
        return Response.success(organizationService.getAllOrganizations(page, size, textSearch));
    }

    @Operation(summary = "Xem chi tiết tổ chức ",description = "Lấy thông tin chi tiết của một tổ chức theo ID")
    @GetMapping("/{organizationId}")
    public Response<?> getOrganization(@PathVariable("organizationId") Integer organizationId){
        return Response.success(organizationService.getOrganizationById(organizationId));
    }

    @GetMapping("/internal/get-by-email-customer")
    @Operation(summary = "Lấy thông tin tổ chức theo khách hàng ",description = "Lấy thông tin tổ chức theo khách hàng (dùng cho nội bộ service)")
    public OrganizationResponseDTO getOrganizationByCustomerEmail(@RequestParam("customerEmail") String customerEmail){
        return organizationService.getOrganizationByCustomerEmail(customerEmail);
    }

}
