package com.ec.notification.mapper;

import com.ec.notification.model.dto.NoticeDTO;
import com.ec.notification.model.entity.Notice;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NoticeMapper {

    Notice toEntity(NoticeDTO noticeDTO);

    NoticeDTO toDTO(Notice notice);
}
