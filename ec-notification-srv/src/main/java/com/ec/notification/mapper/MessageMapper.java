package com.ec.notification.mapper;

import com.ec.notification.model.DTO.MessageDTO;
import com.ec.notification.model.entity.Message;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    Message toEntity(MessageDTO messageDTO);

    MessageDTO toDTO(Message message);
}
