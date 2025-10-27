package com.ec.contract.mapper;

import com.ec.contract.model.dto.FieldDto;
import com.ec.contract.model.entity.Field;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RecipientMapper.class})
public interface FieldMapper {

    @Mapping(source = "recipient.id", target = "recipientId")
    FieldDto toDto(Field entity);

    List<FieldDto> toDtoList(List<Field> entities);

    List<Field> toEntityList(List<FieldDto> dtos);

    Field toEntity(FieldDto dto);
}
