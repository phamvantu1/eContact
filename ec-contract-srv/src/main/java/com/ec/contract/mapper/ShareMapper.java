package com.ec.contract.mapper;

import com.ec.contract.model.dto.ShareDto;
import com.ec.contract.model.dto.TypeDTO;
import com.ec.contract.model.entity.Share;
import com.ec.contract.model.entity.Type;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShareMapper {

    Share toEntity(ShareDto shareDto);

    ShareDto toDTO(Share share);

    List<ShareDto> toDTOList(List<Share> shares);
}
