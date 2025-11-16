package com.ec.contract.service;

import com.ec.contract.mapper.TemplateFieldMapper;
import com.ec.contract.model.dto.FieldDto;
import com.ec.contract.model.entity.*;
import com.ec.contract.repository.TemplateContractRepository;
import com.ec.contract.repository.TemplateDocumentRepository;
import com.ec.contract.repository.TemplateFieldRepository;
import com.ec.contract.repository.TemplateRecipientRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateFieldService {

    private final TemplateContractRepository templateContractRepository;
    private final TemplateDocumentRepository templateDocumentRepository;
    private final TemplateRecipientRepository templateRecipientRepository;
    private final TemplateFieldMapper templateFieldMapper;
    private final TemplateFieldRepository templateFieldRepository;

    @Transactional
    public List<FieldDto> createFields(List<FieldDto> fieldDtoList){
        try{
            List<TemplateField> fieldList = new ArrayList<>();

            fieldDtoList.forEach(fieldDto -> {

                TemplateContract contract = templateContractRepository.findById(fieldDto.getContractId())
                        .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

                TemplateDocument document = templateDocumentRepository.findById(fieldDto.getDocumentId())
                        .orElseThrow(() -> new CustomException(ResponseCode.DOCUMENT_NOT_FOUND));

                TemplateRecipient recipient = templateRecipientRepository.findById(fieldDto.getRecipientId())
                        .orElseThrow(() -> new CustomException(ResponseCode.RECIPIENT_NOT_FOUND));

                TemplateField field = templateFieldMapper.toEntity(fieldDto);

                fieldList.add(field);
            });

            var result = templateFieldRepository.saveAll(fieldList);

            return templateFieldMapper.toDtoList(result);

        }catch (CustomException e) {
            throw e;
        }catch (Exception e){
            log.error("Error creating fields: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create fields", e);
        }
    }

}
