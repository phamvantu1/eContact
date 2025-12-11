package com.ec.notification.mapper;

import com.ec.notification.model.dto.EmailDTO;
import com.ec.notification.model.entity.Email;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmailMapper {

    Email toEntity(EmailDTO emailDTO);

    EmailDTO toDTO(Email email);

}
