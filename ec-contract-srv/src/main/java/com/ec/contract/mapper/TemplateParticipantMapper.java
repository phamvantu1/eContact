package com.ec.contract.mapper;

import com.ec.contract.model.dto.ParticipantDTO;
import com.ec.contract.model.entity.TemplateParticipant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TemplateRecipientMapper.class})
public interface TemplateParticipantMapper {

    @Mapping(source = "contract.id", target = "contractId")
    ParticipantDTO toDto(TemplateParticipant entity);

    List<ParticipantDTO> toDtoList(List<TemplateParticipant> entities);
}