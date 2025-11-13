package com.ec.contract.mapper;

import com.ec.contract.model.dto.RecipientDTO;
import com.ec.contract.model.entity.Recipient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {FieldMapper.class})
public interface RecipientMapper {

    @Mapping(source = "participant.id", target = "participantId")
    @Mapping(target = "participant", ignore = true) // tránh vòng lặp
    RecipientDTO toDto(Recipient entity);
}

