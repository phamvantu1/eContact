package com.ec.contract.mapper;

import com.ec.contract.model.dto.response.ContractRefResponseDTO;
import com.ec.contract.model.entity.ContractRef;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContractRefMapper {

    @Mapping(source = "contract.id", target = "contractId") // nếu muốn chỉ lấy id thì đổi thành contract.id → contractId
    ContractRefResponseDTO toDto(ContractRef entity);
}