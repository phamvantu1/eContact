package com.ec.contract.service;

import com.ec.contract.constant.RecipientRole;
import com.ec.contract.constant.RecipientStatus;
import com.ec.contract.mapper.ContractMapper;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private final BpmnService bpmnService;
    private final ContractMapper contractMapper;

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

                    Contract contract = contractRepository.findById(participant.getContractId())
                            .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

                    List<Participant> listParticipants = participantRepository.findByContractIdOrderByOrderingAsc(contract.getId())
                            .stream().toList();

                    for (Participant par : listParticipants) {
                        Set<Recipient> recipientSet = par.getRecipients();

                        for (Recipient reci : recipientSet) {
                            Collection<Field> fieldCollection = fieldRepository.findAllByRecipientId(reci.getId());
                            reci.setFields(Set.copyOf(fieldCollection));
                        }

                        par.setRecipients(recipientSet);
                    }

                    contract.setParticipants(Set.copyOf(listParticipants));

                    ContractResponseDTO contractResponseDTO = contractMapper.toDto(contract);

                    bpmnService.handleCoordinatorService(contractResponseDTO, recipientId);
                }

                return Optional.ofNullable(
                        modelMapper.map(updated, ParticipantDTO.class)
                );
            }
        }catch (CustomException ce){
            log.error("Error updateRecipientForCoordinator: {}", ce.getMessage());
            throw ce;
        }
        catch (Exception e) {
            log.error("Error catch updateRecipientForCoordinator: {}", e.getMessage());
            // TODO: handle exception
        }

        return Optional.empty();
    }


    @Transactional
    public RecipientDTO approval(int recipientId) {

        log.info("approval recipient: {} ", recipientId);

        var recipient = recipientRepository.findById(recipientId);

        if (recipient.isPresent()) {

            try {

                var recipientOptional = recipientService.approval(recipientId);

                return recipientOptional.get();

            } catch (Exception e) {
                log.error("Đã có lỗi xảy ra trong quá trình xử lý hàm process approval", e);
            }
        }
        return null;
    }

}
