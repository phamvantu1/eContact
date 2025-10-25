package com.ec.contract.mapper;

import com.ec.contract.model.dto.response.DocumentResponseDTO;
import com.ec.contract.model.entity.Document;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RecipientMapper.class})
public interface DocumentMapper {

    Document toEntity(Document dto);

    DocumentResponseDTO toDto(Document entity);

    List<DocumentResponseDTO> toDtoList(List<Document> entities);
}
