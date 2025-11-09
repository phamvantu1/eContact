package com.ec.contract.service;

import com.ec.contract.constant.RecipientStatus;
import com.ec.contract.mapper.RecipientMapper;
import com.ec.contract.model.dto.RecipientDTO;
import com.ec.contract.model.entity.Recipient;
import com.ec.contract.repository.FieldRepository;
import com.ec.contract.repository.RecipientRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipientService {

    private final RecipientRepository recipientRepository;
    private final RecipientMapper recipientMapper;
    private final FieldRepository fieldRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public RecipientDTO getRecipientById(Integer recipientId) {
        try{
            Recipient recipient = recipientRepository.findById(recipientId)
                    .orElseThrow(() -> new CustomException(ResponseCode.RECIPIENT_NOT_FOUND));

            log.info("Fetched recipient entity: {}", recipient);

            return recipientMapper.toDto(recipient);

        } catch (CustomException ex){
            log.error("Custom error fetching recipient by id: {}", ex.getMessage());
            throw ex;
        }
        catch (Exception e) {
            log.error("Error fetching recipient by id: {}",  e.getMessage());
            throw new RuntimeException("Failed to fetch recipient by id", e);
        }
    }

    public Optional<RecipientDTO> changeRecipientProcessing(int id) {
       try{
           final var recipientOptional = recipientRepository.findById(id);
           if (recipientOptional.isPresent()) {
               final var recipient = recipientOptional.get();
               recipient.setStatus(RecipientStatus.PROCESSING.getDbVal());

               final var updated = recipientRepository.save(recipient);
               return Optional.of(modelMapper.map(updated, RecipientDTO.class));
           }
       }catch (Exception e){
           log.error("Error updating recipient status to PROCESSING for id {}: {}", id, e.getMessage());
       }
       return Optional.empty();
    }
}
