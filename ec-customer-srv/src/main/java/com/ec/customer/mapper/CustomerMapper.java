package com.ec.customer.mapper;

import com.ec.customer.model.DTO.response.RoleResponseDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.ec.customer.model.DTO.request.CustomerRequestDTO;
import com.ec.customer.model.DTO.response.CustomerResponseDTO;
import com.ec.customer.model.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    // Map từ RequestDTO → Entity (tạo mới)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "signImage", ignore = true)
    Customer toEntity(CustomerRequestDTO dto);

    // Map từ RequestDTO → Entity (cập nhật)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "signImage", ignore = true)
    void updateEntityFromDto(CustomerRequestDTO dto, @MappingTarget Customer customer);

//    // Map từ Entity → ResponseDTO
//    @Mapping(target = "organizationId",
//            expression = "java(customer.getOrganization() != null ? customer.getOrganization().getId().intValue() : null)")

    @Mapping(target = "organizationId", expression = "java(mapOrganizationId(customer))")
    @Mapping(target = "roles", expression = "java(mapRoles(customer))")
    CustomerResponseDTO toResponseDTO(Customer customer);

    // helper methods
    default Integer mapOrganizationId(Customer customer) {
        return customer.getOrganization() != null
                ? customer.getOrganization().getId().intValue()
                : null;
    }

    default List<RoleResponseDTO> mapRoles(Customer customer) {
        if (customer.getRoles() == null) return null;
        return customer.getRoles().stream()
                .map(role -> new RoleResponseDTO(role.getId(), role.getName()))
                .collect(Collectors.toList());
    }
}
