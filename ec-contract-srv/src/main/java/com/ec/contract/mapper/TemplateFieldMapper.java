package com.ec.contract.mapper;

import com.ec.contract.model.dto.FieldDto;
import com.ec.contract.model.entity.TemplateField;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TemplateRecipientMapper.class})
public interface TemplateFieldMapper {

    @Mapping(source = "recipient.id", target = "recipientId")
    FieldDto toDto(TemplateField entity);

    List<FieldDto> toDtoList(List<TemplateField> entities);

    List<TemplateField> toEntityList(List<FieldDto> dtos);

    TemplateField toEntity(FieldDto dto);
}
