package com.ec.contract.mapper;

import com.ec.contract.model.dto.RecipientDTO;
import com.ec.contract.model.entity.Recipient;
import com.ec.contract.model.entity.TemplateRecipient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {TemplateFieldMapper.class})
public interface TemplateRecipientMapper {

    @Mapping(source = "participant.id", target = "participantId")
    @Mapping(target = "participant", ignore = true) // tránh vòng lặp
    RecipientDTO toDto(TemplateRecipient entity);
}

