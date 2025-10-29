package com.ec.customer.mapper;


import com.ec.customer.model.DTO.request.OrganizationRequestDTO;
import com.ec.customer.model.DTO.response.OrganizationResponseDTO;
import com.ec.customer.model.entity.Organization;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    Organization toEntity(OrganizationRequestDTO requestDTO);

    OrganizationResponseDTO toDTO(Organization organization);

    List<OrganizationResponseDTO> toDTOList(List<Organization> organizations);
}

