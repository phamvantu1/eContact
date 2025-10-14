package com.ec.customer.controller;

import com.ec.customer.model.DTO.request.CustomerRequestDTO;
import com.ec.customer.service.CustommerService;
import com.ec.library.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private CustommerService custommerService;
    @PostMapping("/create-customer")
    public Response<?> createCustomer(@Valid CustomerRequestDTO customerRequestDTO){

        return Response.success(custommerService.createCustomer(customerRequestDTO));

    }
}
