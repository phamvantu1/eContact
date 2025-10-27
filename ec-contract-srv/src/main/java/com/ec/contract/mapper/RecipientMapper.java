package com.ec.contract.mapper;

import com.ec.contract.model.dto.RecipientDTO;
import com.ec.contract.model.dto.SignTypeDTO;
import com.ec.contract.model.entity.Recipient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {FieldMapper.class})
public interface RecipientMapper {

    @Mapping(source = "participant.id", target = "participantId")
    RecipientDTO toDto(Recipient entity);
}

