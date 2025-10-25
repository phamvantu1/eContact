package com.ec.contract.mapper;

import com.ec.contract.model.dto.response.ParticipantResponseDTO;
import com.ec.contract.model.entity.Participant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {RecipientMapper.class})
public interface ParticipantMapper {

    @Mapping(source = "contract.id", target = "contractId")
    ParticipantResponseDTO toDto(Participant entity);
}