package com.ec.customer.controller;

import com.ec.customer.model.DTO.request.CustomerRequestDTO;
import com.ec.customer.service.CustomerService;
import com.ec.library.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Customer API", description = "Quản lý thông tin khách hàng")
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Tạo mới khách hàng", description = "Thêm mới một khách hàng vào hệ thống")
    @PostMapping("/create-customer")
    public Response<?> createCustomer(@Valid @RequestBody CustomerRequestDTO customerRequestDTO){
        return Response.success(customerService.createCustomer(customerRequestDTO));
    }

    @Operation(summary = "Cập nhật thông tin khách hàng", description = "Cập nhật dữ liệu khách hàng theo ID")
    @PutMapping("/update-customer/{customerId}")
    public Response<?> updateCustomer(@Valid @RequestBody CustomerRequestDTO customerRequestDTO){
        return Response.success(customerService.createCustomer(customerRequestDTO));
    }

    @Operation(summary = "Xóa khách hàng", description = "Xóa khách hàng theo ID")
    @DeleteMapping("/delete-customer/{customerId}")
    public Response<?> deleteCustomer(@PathVariable Long customerId){
        return Response.success(customerService.deleteCustomer(customerId));
    }

    @Operation(summary = "Xem chi tiết khách hàng", description = "Lấy thông tin chi tiết của một khách hàng theo ID")
    @GetMapping("/get-customer/{customerId}")
    public Response<?> getCustomer(@PathVariable Long customerId){
        return Response.success(customerService.getCustomerById(customerId));
    }

    @Operation(summary = "Danh sách khách hàng", description = "Lấy danh sách khách hàng có phân trang và tìm kiếm")
    @GetMapping("/get-all-customer")
    public Response<?> getAllCustomer(@RequestParam("page") int page,
                                      @RequestParam("size") int size,
                                      @RequestParam("textSearch") String textSearch,
                                      @RequestParam("organizationId") Long organizationId){
        return Response.success(customerService.getAllCustomer(page, size,textSearch, organizationId));
    }
    @GetMapping("/get-by-email")
    public Response<?> getCustomerByEmail(@RequestParam(name = "email") String email){
        return Response.success(customerService.getCustomerByEmail(email));
    }

    @PostMapping("/register")
    public Response<?> registerCustomer( @RequestBody CustomerRequestDTO customerRequestDTO){
        return Response.success(customerService.registerCustomer(customerRequestDTO));
    }

    @PostMapping("/test")
    public Response<?> test(){
        return Response.success("hah");
    }
}
