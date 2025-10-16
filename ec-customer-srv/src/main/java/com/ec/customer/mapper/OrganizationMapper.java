package com.ec.customer.mapper;

import com.ec.customer.model.DTO.response.OrganizationResponseDTO;
import com.ec.customer.model.entity.Organization;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OrganizationMapper {

    // --- Chuyển từ Entity -> DTO ---
    public OrganizationResponseDTO toResponseDTO(Organization organization) {
        if (organization == null) return null;

        OrganizationResponseDTO dto = new OrganizationResponseDTO();
        dto.setId(organization.getId());
        dto.setName(organization.getName());
        dto.setEmail(organization.getEmail());
        dto.setStatus(organization.getStatus());
        dto.setTaxCode(organization.getTaxCode());

        // map đệ quy children
        dto.setChildren(mapChildren(organization.getChildren()));

        return dto;
    }

    // --- map danh sách con ---
    private Set<OrganizationResponseDTO> mapChildren(Set<Organization> children) {
        if (children == null || children.isEmpty()) return null;

        return children.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toSet());
    }
}
