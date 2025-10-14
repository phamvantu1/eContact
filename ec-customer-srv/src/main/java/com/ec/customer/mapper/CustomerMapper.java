package com.ec.customer.mapper;

import com.ec.customer.model.DTO.request.CustomerRequestDTO;
import com.ec.customer.model.DTO.response.CustomerResponseDTO;
import com.ec.customer.model.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

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

    // Map từ Entity → ResponseDTO
    @Mapping(target = "organizationId", expression = "java(customer.getOrganization() != null ? customer.getOrganization().getId().intValue() : null)")
    @Mapping(target = "roleId", expression = "java(customer.getRoles() != null && !customer.getRoles().isEmpty() ? customer.getRoles().iterator().next().getId().intValue() : null)")
    CustomerResponseDTO toResponseDTO(Customer customer);
}