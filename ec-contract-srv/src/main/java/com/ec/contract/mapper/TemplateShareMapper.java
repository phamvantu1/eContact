package com.ec.contract.mapper;

import com.ec.contract.model.dto.ShareDto;
import com.ec.contract.model.entity.TemplateShare;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TemplateShareMapper {

    TemplateShare toEntity(ShareDto shareDto);

    ShareDto toDTO(TemplateShare share);

    List<ShareDto> toDTOList(List<TemplateShare> shares);
}
