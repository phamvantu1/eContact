package com.ec.customer.mapper;

import com.ec.customer.model.DTO.response.OrganizationResponseDTO;
import com.ec.customer.model.entity.Organization;
import org.mapstruct.*;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    @Mapping(target = "children", expression = "java(mapChildren(organization.getChildren()))")
    OrganizationResponseDTO toResponseDTO(Organization organization);

    default Set<OrganizationResponseDTO> mapChildren(Set<Organization> children) {
        if (children == null || children.isEmpty()) {
            return null;
        }
        return children.stream()
                .map(this::toResponseDTO)
                .collect(java.util.stream.Collectors.toSet());
    }
}