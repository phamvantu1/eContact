package com.ec.contract.mapper;

import com.ec.contract.model.dto.response.DocumentResponseDTO;
import com.ec.contract.model.entity.TemplateDocument;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TemplateRecipientMapper.class})
public interface TemplateDocumentMapper {

    TemplateDocument toEntity(DocumentResponseDTO dto);

    DocumentResponseDTO toDto(TemplateDocument entity);

    List<DocumentResponseDTO> toDtoList(List<TemplateDocument> entities);
}
