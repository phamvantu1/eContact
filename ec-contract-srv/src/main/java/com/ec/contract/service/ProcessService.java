package com.ec.contract.service;

import com.ec.contract.constant.RecipientRole;
import com.ec.contract.constant.RecipientStatus;
import com.ec.contract.mapper.ContractMapper;
import com.ec.contract.mapper.ParticipantMapper;
import com.ec.contract.model.dto.ParticipantDTO;
import com.ec.contract.model.dto.RecipientDTO;
import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.contract.model.entity.Contract;
import com.ec.contract.model.entity.Field;
import com.ec.contract.model.entity.Participant;
import com.ec.contract.model.entity.Recipient;
import com.ec.contract.repository.ContractRepository;
import com.ec.contract.repository.FieldRepository;
import com.ec.contract.repository.ParticipantRepository;
import com.ec.contract.repository.RecipientRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessService {

    private final ParticipantRepository participantRepository;
    private final FieldRepository fieldRepository;
    private final RecipientRepository recipientRepository;
    private final RecipientService recipientService;
    private final ContractRepository contractRepository;
    private final BpmnService bpmnService;
    private final ContractMapper contractMapper;
    private final ParticipantMapper participantMapper;

    @Transactional
    public ParticipantDTO updateRecipientForCoordinator(Authentication authentication,
                                                                  int participantId,
                                                                  int recipientId,
                                                                  Collection<RecipientDTO> recipientDtoCollection) {
        try {
            final var participantOptional = participantRepository.findById(participantId);

            if (participantOptional.isPresent()) {
                final var participant = participantOptional.get();

                //xóa toàn bộ khách hàng xử lý hồ sơ
                participant.getRecipients()
                        .removeIf(recipient -> true);

                //Thêm mới khách hàng xử lý
                for (var recipientDto : recipientDtoCollection) {
                    var recipient = new Recipient();
                    BeanUtils.copyProperties(
                            recipientDto, recipient,
                            "fields"
                    );

                    participant.addRecipient(recipient);
                }

                final var updated = participantRepository.save(participant);

                final var recipientOptional = recipientRepository.findById(recipientId);
                if (recipientOptional.isPresent()) {
                    final var recipient = recipientOptional.get();

                    // update recipient status
                    recipient.setStatus(RecipientStatus.APPROVAL.getDbVal());
                    recipient.setProcessAt(LocalDateTime.now());
                    recipientRepository.save(recipient);

                    Contract contract = contractRepository.findById(participant.getContractId())
                            .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

                    ContractResponseDTO contractResponseDTO = contractMapper.toDto(contract);

                    bpmnService.handleCoordinatorService(contractResponseDTO, recipientId);
                }

                return participantMapper.toDto(updated);
            }
        }catch (CustomException ce){
            log.error("Error updateRecipientForCoordinator: {}", ce.getMessage());
            throw ce;
        }
        catch (Exception e) {
            log.error("Error catch updateRecipientForCoordinator: {}", e.getMessage());
            // TODO: handle exception
            throw e;
        }

        return null;

    }

    @Transactional
    public RecipientDTO approval(int recipientId) {

        log.info("approval recipient: {} ", recipientId);

        var recipient = recipientRepository.findById(recipientId);

        if (recipient.isPresent()) {

            try {
                return recipientService.approval(recipientId, recipient.get().getRole());
            } catch (Exception e) {
                log.error("Đã có lỗi xảy ra trong quá trình xử lý hàm process approval", e);
            }
        }
        return null;
    }

}
