package com.ec.customer.controller;

import com.ec.customer.model.DTO.request.OrganizationRequestDTO;
import com.ec.customer.service.OrganizationService;
import com.ec.library.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping("/create")
    public Response<?> createOrganization(@Valid @RequestBody OrganizationRequestDTO organizationRequestDTO){
        return Response.success(organizationService.createOrganization(organizationRequestDTO));
    }

    @DeleteMapping("/delete/{organizationId}")
    public Response<?> deleteOrganization(@PathVariable("organizationId") Integer organizationId){
        return Response.success(organizationService.deleteOrganization(organizationId));
    }

    @PutMapping("/update/{organizationId}")
    public Response<?> updateOrganization(@Valid @RequestBody OrganizationRequestDTO organizationRequestDTO,
                                         @PathVariable("organizationId") Integer organizationId){
        return Response.success(organizationService.updateOrganization(organizationId, organizationRequestDTO));
    }

    @GetMapping("/get-all")
    public Response<?> getAllOrganization(@RequestParam(name = "textSearch", required = false, defaultValue = "") String textSearch,
                                          @RequestParam(name = "page" , required = false, defaultValue = "0") int page,
                                          @RequestParam(name = "size", required = false, defaultValue = "10") int size){
        return Response.success(organizationService.getAllOrganizations(page, size, textSearch));
    }

    @GetMapping("/{organizationId}")
    public Response<?> getOrganization(@PathVariable("organizationId") Integer organizationId){
        return Response.success(organizationService.getOrganizationById(organizationId));
    }

}
