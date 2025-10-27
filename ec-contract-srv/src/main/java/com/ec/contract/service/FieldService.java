package com.ec.contract.service;

import com.ec.contract.mapper.FieldMapper;
import com.ec.contract.model.dto.FieldDto;
import com.ec.contract.model.entity.Contract;
import com.ec.contract.model.entity.Document;
import com.ec.contract.model.entity.Field;
import com.ec.contract.model.entity.Recipient;
import com.ec.contract.repository.ContractRepository;
import com.ec.contract.repository.DocumentRepository;
import com.ec.contract.repository.FieldRepository;
import com.ec.contract.repository.RecipientRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FieldService {

    private final ContractRepository contractRepository;
    private final FieldRepository fieldRepository;
    private final ParticipantService participantService;
    private final RecipientService recipientService;
    private final RecipientRepository recipientRepository;
    private final FieldMapper fieldMapper;
    private final DocumentRepository documentRepository;

    @Transactional
    public List<FieldDto> createFields(List<FieldDto> fieldDtoList){
        try{
            List<Field> fieldList = new ArrayList<>();

            fieldDtoList.forEach(fieldDto -> {

                Contract contract = contractRepository.findById(fieldDto.getContractId())
                        .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

                Document document = documentRepository.findById(fieldDto.getDocumentId())
                        .orElseThrow(() -> new CustomException(ResponseCode.DOCUMENT_NOT_FOUND));

                Recipient recipient = recipientRepository.findById(fieldDto.getRecipientId())
                        .orElseThrow(() -> new CustomException(ResponseCode.RECIPIENT_NOT_FOUND));

                Field field = fieldMapper.toEntity(fieldDto);

                fieldList.add(field);
            });

            var result = fieldRepository.saveAll(fieldList);

            return fieldMapper.toDtoList(result);

        }catch (CustomException e) {
            throw e;
        }catch (Exception e){
            log.error("Error creating fields: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create fields", e);
        }
    }

}
