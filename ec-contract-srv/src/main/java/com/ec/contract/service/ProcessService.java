package com.ec.contract.service;

import com.ec.contract.constant.RecipientRole;
import com.ec.contract.constant.RecipientStatus;
import com.ec.contract.model.dto.ParticipantDTO;
import com.ec.contract.model.dto.RecipientDTO;
import com.ec.contract.model.entity.Recipient;
import com.ec.contract.repository.ContractRepository;
import com.ec.contract.repository.FieldRepository;
import com.ec.contract.repository.ParticipantRepository;
import com.ec.contract.repository.RecipientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessService {

    private final DocumentService documentService;
    private final ParticipantRepository participantRepository;
    private final FieldRepository fieldRepository;
    private final RecipientRepository recipientRepository;
    private final FieldService fieldService;
    private final ModelMapper modelMapper;
    private final CustomerService customerService;
    private final RecipientService recipientService;
    private final ContractRepository contractRepository;
    private final ObjectMapper objectMapper;
    private final ContractService contractService;

    @Transactional
    public Optional<ParticipantDTO> updateRecipientForCoordinator(Authentication authentication,
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

                    // call bpmn
                    var workflowDto = WorkflowDto.builder()
                            .contractId(participant.getContractId())
                            .approveType(ContractApproveType.APPROVAL.getDbVal())
                            .actionType(recipient.getRole().getDbVal())
                            .participantId(updated.getId())
                            .recipientId(recipient.getId())
                            .build();

                    bpmService.startWorkflow(workflowDto);
                }

                return Optional.ofNullable(
                        modelMapper.map(updated, ParticipantDto.class)
                );
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        return Optional.empty();
    }

}
