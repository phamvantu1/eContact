package com.ec.contract.mapper;

import com.ec.contract.model.dto.request.ContractRequestDTO;
import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.contract.model.entity.Contract;
import com.ec.contract.model.entity.TemplateContract;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = { TemplateParticipantMapper.class}
)
public interface TemplateContractMapper {

    // Entity -> DTO
    ContractResponseDTO toDto(TemplateContract entity);

    // List<Entity> -> List<DTO>
    List<ContractResponseDTO> toDtoList(List<TemplateContract> entities);

    TemplateContract toEntity(ContractRequestDTO dto);
}