package com.ec.customer.mapper;

import com.ec.customer.model.DTO.request.CustomerRequestDTO;
import com.ec.customer.model.DTO.response.CustomerResponseDTO;
import com.ec.customer.model.DTO.response.RoleResponseDTO;
import com.ec.customer.model.entity.Customer;
import com.ec.customer.model.entity.Organization;
import com.ec.customer.model.entity.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CustomerMapper {

    // --- Map từ Request DTO -> Entity ---
    public Customer toEntity(CustomerRequestDTO dto) {
        if (dto == null) return null;

        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPassword(dto.getPassword());
        customer.setPhone(dto.getPhone());
        customer.setBirthday(dto.getBirthday());
        customer.setGender(dto.getGender());
        customer.setStatus(dto.getStatus());
        customer.setTaxCode(dto.getTaxCode());

        // Map Organization
        if (dto.getOrganizationId() != null) {
            Organization organization = new Organization();
            organization.setId(dto.getOrganizationId());
            customer.setOrganization(organization);
        }

        // Map Role
        if (dto.getRoleId() != null) {
            Role role = new Role();
            role.setId(dto.getRoleId());
            customer.setRoles(Set.of(role));
        }

        return customer;
    }

    // --- Cập nhật Entity từ Request DTO ---
    public void updateEntityFromDto(CustomerRequestDTO dto, Customer customer) {
        if (dto == null || customer == null) return;

        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
//        customer.setPassword(dto.getPassword());
        customer.setPhone(dto.getPhone());
        customer.setBirthday(dto.getBirthday());
        customer.setGender(dto.getGender());
        customer.setStatus(dto.getStatus());
        customer.setTaxCode(dto.getTaxCode());

        if (dto.getOrganizationId() != null) {
            Organization organization = new Organization();
            organization.setId(dto.getOrganizationId());
            customer.setOrganization(organization);
        }

        if (dto.getRoleId() != null) {
            Role role = new Role();
            role.setId(dto.getRoleId());
            customer.setRoles(Set.of(role));
        }
    }

    // --- Map từ Entity -> Response DTO ---
    public CustomerResponseDTO toResponseDTO(Customer entity) {
        if (entity == null) return null;

        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setPassword(entity.getPassword());
        dto.setPhone(entity.getPhone());
        dto.setBirthday(entity.getBirthday());
        dto.setGender(entity.getGender());
        dto.setStatus(entity.getStatus());
        dto.setTaxCode(entity.getTaxCode());

        if (entity.getOrganization() != null) {
            dto.setOrganizationId(entity.getOrganization().getId());
        }

        if (entity.getRoles() != null) {
            dto.setRoles(mapRolesToDto(entity.getRoles()));
        }

        return dto;
    }

    // --- Helper method ---
    private List<RoleResponseDTO> mapRolesToDto(Set<Role> roles) {
        return roles.stream().map(role -> {
            RoleResponseDTO dto = new RoleResponseDTO();
            dto.setId(role.getId());
            dto.setName(role.getName());
            dto.setPermissions(role.getPermissions());
            return dto;
        }).collect(Collectors.toList());
    }
}
