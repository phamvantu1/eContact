package com.ec.contract.service;

import com.ec.contract.mapper.RecipientMapper;
import com.ec.contract.model.dto.RecipientDTO;
import com.ec.contract.model.entity.Field;
import com.ec.contract.model.entity.Recipient;
import com.ec.contract.repository.FieldRepository;
import com.ec.contract.repository.RecipientRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;


@Service
@RequiredArgsConstructor
@Slf4j
public class RecipientService {

    private final RecipientRepository recipientRepository;
    private final RecipientMapper recipientMapper;
    private final FieldRepository fieldRepository;

    @Transactional(readOnly = true)
    public RecipientDTO getRecipientById(Integer recipientId) {
        try{
            Recipient recipient = recipientRepository.findById(recipientId)
                    .orElseThrow(() -> new CustomException(ResponseCode.RECIPIENT_NOT_FOUND));

            Collection<Field> fields = fieldRepository.findByRecipientId(recipientId);

//            recipient.setFields(new HashSet<>(fields));
//            log.info("Fetched recipient: {}", recipient);

            return recipientMapper.toDto(recipient);
        } catch (CustomException ex){
            log.error("Custom error fetching recipient by id: {}", ex.getMessage());
            throw ex;
        }
        catch (Exception e) {
            log.error("Error fetching recipient by id: {}",  e.getMessage());
            return null;
        }
    }
}
