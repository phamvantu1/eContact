package com.ec.contract.mapper;

import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.contract.model.entity.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {ContractRefMapper.class, ParticipantMapper.class}
)
public interface ContractMapper {

    // Entity -> DTO
    ContractResponseDTO toDto(Contract entity);

    // List<Entity> -> List<DTO>
    List<ContractResponseDTO> toDtoList(List<Contract> entities);
}