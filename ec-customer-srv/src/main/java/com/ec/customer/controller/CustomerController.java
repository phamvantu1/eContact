package com.ec.customer.controller;

import com.ec.customer.model.DTO.request.ChangePasswordDTO;
import com.ec.customer.model.DTO.request.CustomerRequestDTO;
import com.ec.customer.service.CustomerService;
import com.ec.library.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Customer API", description = "Quản lý thông tin user")
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Tạo mới user", description = "Thêm mới một user vào hệ thống")
    @PostMapping("/create-customer")
    public Response<?> createCustomer(@Valid @RequestBody CustomerRequestDTO customerRequestDTO){
        return Response.success(customerService.createCustomer(customerRequestDTO));
    }

    @Operation(summary = "Cập nhật thông tin user", description = "Cập nhật dữ liệu user theo ID")
    @PutMapping("/update-customer/{customerId}")
    public Response<?> updateCustomer(@Valid @RequestBody CustomerRequestDTO customerRequestDTO,
                                      @PathVariable("customerId") Integer customerId){
        return Response.success(customerService.updateCustomer(customerId, customerRequestDTO));
    }

    @Operation(summary = "Xóa user", description = "Xóa user theo ID")
    @DeleteMapping("/delete-customer/{customerId}")
    public Response<?> deleteCustomer(@PathVariable("customerId") Integer customerId){
        return Response.success(customerService.deleteCustomer(customerId));
    }

    @Operation(summary = "Xem chi tiết user", description = "Lấy thông tin chi tiết của một user theo ID")
    @GetMapping("/{customerId}")
    public Response<?> getCustomer(@PathVariable("customerId") Integer customerId){
        return Response.success(customerService.getCustomerById(customerId));
    }

    @Operation(summary = "Danh sách user", description = "Lấy danh user hàng có phân trang và tìm kiếm")
    @GetMapping("/get-all-customer")
    public Response<?> getAllCustomer(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                      @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                                      @RequestParam(name = "textSearch", required = false) String textSearch,
                                      @RequestParam(name = "organizationId", required = false) Integer organizationId){
        return Response.success(customerService.getAllCustomer(page, size,textSearch, organizationId));
    }

    @Operation(summary = "Lấy thông tin user theo email")
    @GetMapping("/internal/get-by-email")
    public Response<?> getCustomerByEmail(@RequestParam(name = "email") String email){
        return Response.success(customerService.getCustomerByEmail(email));
    }

    @Operation(summary = "Đăng ký user ( FE không dùng )", description = "Dùng để call service từ auth-service")
    @PostMapping("/internal/register")
    public Response<?> registerCustomer( @RequestBody CustomerRequestDTO customerRequestDTO){
        return Response.success(customerService.registerCustomer(customerRequestDTO));
    }

    @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu cho user theo ID")
    @PutMapping("/change-password/{customerId}")
    public Response<?> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO,
                                      @PathVariable("customerId") Integer customerId){
        return Response.success(customerService.changePassword(customerId, changePasswordDTO));
    }


}
