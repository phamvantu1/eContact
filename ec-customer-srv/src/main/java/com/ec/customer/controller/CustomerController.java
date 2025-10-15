package com.ec.customer.controller;

import com.ec.customer.model.DTO.request.CustomerRequestDTO;
import com.ec.customer.service.CustommerService;
import com.ec.library.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustommerService custommerService;
    @PostMapping("/create-customer")
    public Response<?> createCustomer(@Valid @RequestBody CustomerRequestDTO customerRequestDTO){

        return Response.success(custommerService.createCustomer(customerRequestDTO));

    }

    @GetMapping("/get-by-email")
    public Response<?> getCustomerByEmail(@RequestParam(name = "email") String email){
        return Response.success(custommerService.getCustomerByEmail(email));
    }

    @PostMapping("/register")
    public Response<?> registerCustomer( @RequestBody CustomerRequestDTO customerRequestDTO){
        return Response.success(custommerService.registerCustomer(customerRequestDTO));
    }

    @PostMapping("/login")
    public Response<?> login(){
        return Response.success("hah");
    }
}
