package com.ec.contract.service;

import com.ec.contract.mapper.TemplateRecipientMapper;
import com.ec.contract.model.dto.RecipientDTO;
import com.ec.contract.model.entity.Recipient;
import com.ec.contract.model.entity.TemplateRecipient;
import com.ec.contract.repository.TemplateRecipientRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateRecipientService {

    private final TemplateRecipientRepository templateRecipientRepository;
    private final TemplateRecipientMapper templateRecipientMapper;

    @Transactional(readOnly = true)
    public RecipientDTO getRecipientById(Integer recipientId) {
        try {
            TemplateRecipient recipient = templateRecipientRepository.findById(recipientId)
                    .orElseThrow(() -> new CustomException(ResponseCode.RECIPIENT_NOT_FOUND));

            log.info("Fetched template recipient entity: {}", recipient);

            return templateRecipientMapper.toDto(recipient);

        } catch (CustomException ex) {
            log.error("Custom error fetching recipient by id: {}", ex.getMessage());
            throw ex;
        } catch (Exception e) {
            log.error("Error fetching recipient by id: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch recipient by id", e);
        }
    }

}
