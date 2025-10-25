package com.ec.contract.mapper;

import com.ec.contract.model.dto.ParticipantDTO;
import com.ec.contract.model.entity.Participant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RecipientMapper.class})
public interface ParticipantMapper {

    @Mapping(source = "contract.id", target = "contractId")
    ParticipantDTO toDto(Participant entity);

    List<ParticipantDTO> toDtoList(List<Participant> entities);
}