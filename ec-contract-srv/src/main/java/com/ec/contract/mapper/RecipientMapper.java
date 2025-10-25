package com.ec.contract.mapper;

import com.ec.contract.model.dto.response.RecipientResponseDTO;
import com.ec.contract.model.entity.Recipient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecipientMapper {

    @Mapping(source = "participant.id", target = "participantId")
    RecipientResponseDTO toDto(Recipient entity);
}
