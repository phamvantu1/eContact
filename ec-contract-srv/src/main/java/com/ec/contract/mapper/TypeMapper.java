package com.ec.contract.mapper;

import com.ec.contract.model.dto.TypeDTO;
import com.ec.contract.model.entity.Type;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TypeMapper {

    Type toEntity(TypeDTO typeDTO);

    TypeDTO toDTO(Type type);

    List<TypeDTO> toDTOList(List<Type> type);
}
