package com.ec.contract.service;

import com.ec.contract.constant.RecipientStatus;
import com.ec.contract.mapper.ContractMapper;
import com.ec.contract.mapper.ParticipantMapper;
import com.ec.contract.mapper.RecipientMapper;
import com.ec.contract.model.dto.ContractChangeStatusRequest;
import com.ec.contract.model.dto.ParticipantDTO;
import com.ec.contract.model.dto.RecipientDTO;
import com.ec.contract.model.dto.SendEmailDTO;
import com.ec.contract.model.dto.request.AuthorizeDTO;
import com.ec.contract.model.dto.request.SendEmailRequestDTO;
import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.contract.model.entity.Contract;
import com.ec.contract.model.entity.Field;
import com.ec.contract.model.entity.Participant;
import com.ec.contract.model.entity.Recipient;
import com.ec.contract.repository.ContractRepository;
import com.ec.contract.repository.FieldRepository;
import com.ec.contract.repository.ParticipantRepository;
import com.ec.contract.repository.RecipientRepository;
import com.ec.library.constants.CommonConstants;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessService {

    private final ParticipantRepository participantRepository;
    private final RecipientRepository recipientRepository;
    private final RecipientService recipientService;
    private final ContractRepository contractRepository;
    private final BpmnService bpmnService;
    private final ContractMapper contractMapper;
    private final ParticipantMapper participantMapper;
    private final RecipientMapper recipientMapper;
    private final NotificationService notificationService;
    private final FieldRepository fieldRepository;

    @Transactional
    public ParticipantDTO updateRecipientForCoordinator(Authentication authentication,
                                                        int participantId,
                                                        int recipientId,
                                                        Collection<RecipientDTO> recipientDtoCollection) {
        try {
            final var participantOptional = participantRepository.findById(participantId);

            Recipient recipientOptional = recipientRepository.findById(recipientId)
                    .orElseThrow(() -> new CustomException(ResponseCode.RECIPIENT_NOT_FOUND));

            if (participantOptional.isPresent()) {

                final var participant = participantOptional.get();

                // Lấy danh sách recipient hiện tại
                Set<Recipient> existingRecipients = participant.getRecipients() != null
                        ? participant.getRecipients()
                        : new HashSet<>();

                Set<Recipient> updatedRecipients = new HashSet<>();

                //Thêm mới khách hàng xử lý
                for (var recipientDto : recipientDtoCollection) {

                    Recipient recipient;

                    if (recipientDto.getId() != null) {
                        // Tìm recipient cũ trong danh sách hiện tại
                        recipient = existingRecipients.stream()
                                .filter(r -> r.getId().equals(recipientDto.getId()))
                                .findFirst()
                                .orElse(new Recipient());
                    }  else if( recipientDto.getEmail() != null ){
                        recipient = existingRecipients.stream()
                                .filter(r -> r.getEmail().equals(recipientDto.getEmail()))
                                .findFirst()
                                .orElse(new Recipient());
                    } else {
                        recipient = new Recipient();
                    }

                    if (recipient.getEmail() == null){
                        BeanUtils.copyProperties(recipientDto, recipient,
                                "fields", "signType", "role", "status");

                    }

                    recipient.setSignType(recipientDto.getSignType());
                    recipient.setRole(recipientDto.getRole());
                    recipient.setStatus(recipientDto.getStatus());
                    recipient.setParticipant(participant);

                    updatedRecipients.add(recipient);
                }

                // orphanRemoval sẽ tự xóa những recipient cũ không còn trong updatedRecipients
                participant.getRecipients().clear();
                participant.getRecipients().addAll(updatedRecipients);

                final var updated = participantRepository.save(participant);

                if(recipientOptional != null){
                    log.info("this phamtusss jjj ");
                    // update recipient status
                    recipientOptional.setStatus(RecipientStatus.APPROVAL.getDbVal());
                    recipientOptional.setProcessAt(LocalDateTime.now());
                    recipientRepository.save(recipientOptional);

                    Contract contract = contractRepository.findById(participant.getContractId())
                            .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

                    ContractResponseDTO contractResponseDTO = contractMapper.toDto(contract);

                    bpmnService.handleCoordinatorService(contractResponseDTO, recipientId);
                }

                return participantMapper.toDto(updated);
            }
        } catch (CustomException ce) {
            log.error("Error updateRecipientForCoordinator: {}", ce.getMessage());
            throw ce;
        } catch (Exception e) {
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

    @Transactional
    public RecipientDTO rejectContract(int recipientId, ContractChangeStatusRequest reason) {
        try {

            Recipient recipient = recipientRepository.findById(recipientId)
                    .orElseThrow(() -> new CustomException(ResponseCode.RECIPIENT_NOT_FOUND));

            recipient.setStatus(RecipientStatus.APPROVAL.getDbVal());
            recipient.setProcessAt(LocalDateTime.now());
            recipient.setReasonReject(reason.getReason());

            var update = recipientRepository.save(recipient);

            Contract contract = contractRepository.findByRecipientId(recipientId).get();

            bpmnService.rejectContract(contractMapper.toDto(contract), reason);

            return recipientMapper.toDto(update);

        } catch (CustomException ce) {
            log.error("Error rejectContract: {}", ce.getMessage());
            throw ce;
        } catch (Exception e) {
            log.error("Error catch rejectContract: {}", e.getMessage());
            // TODO: handle exception
            throw e;
        }
    }

    @Transactional
    public RecipientDTO authorizeContract(Integer recipientId,
                                          AuthorizeDTO authorizeDTO) {
        try {

            Contract contract = contractRepository.findByRecipientId(recipientId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));
            Recipient oldRecipient = recipientRepository.findById(recipientId)
                    .orElseThrow(() -> new CustomException(ResponseCode.RECIPIENT_NOT_FOUND));
            Collection<Field> field = fieldRepository.findAllByRecipientId(recipientId);

            Recipient newRecipient = new Recipient();
            Field newField = new Field();

            BeanUtils.copyProperties(
                    oldRecipient,
                    newRecipient,
                    "id",
                    "cardId",
                    "processAt",
                    "reasonReject",
                    "fields"
            );
            newRecipient.setCardId(authorizeDTO.getTaxCode());
            newRecipient.setName(authorizeDTO.getName());
            newRecipient.setEmail(authorizeDTO.getEmail());
            newRecipient.setStatus(RecipientStatus.PROCESSING.getDbVal());
            recipientRepository.save(newRecipient);

            oldRecipient.setStatus(RecipientStatus.AUTHORIZE.getDbVal());
            oldRecipient.setProcessAt(LocalDateTime.now());
            oldRecipient.setDelegateTo(newRecipient.getId());
            recipientRepository.save(oldRecipient);

            field.forEach(oldField -> {
                try {
                    BeanUtils.copyProperties(
                            oldField,
                            newField,
                            "id",
                            "recipientId"
                    );
                    newField.setRecipientId(newRecipient.getId());
                    fieldRepository.save(newField);
                } catch (Exception e) {
                    log.error("Error copy field authorizeContract: {}", e.getMessage());
                    throw e;
                }
            });

            // send notice
            SendEmailRequestDTO requestDTO = SendEmailRequestDTO.builder()
                    .subject(CommonConstants.SubjectEmail.AUTHORIZE_CONTRACT)
                    .contractId(contract.getId())
                    .recipientId(newRecipient.getId())
                    .code(CommonConstants.CodeEmail.EMAIL)
                    .actionButton(CommonConstants.ActionButton.VIEW_CONTRACT)
                    .titleEmail(CommonConstants.TitleEmail.AUTHORIZE_CONTRACT)
                    .url(CommonConstants.url.AUTHORIZE_CONTRACT)
                    .build();
            SendEmailDTO emailDTO = notificationService.setSendEmailDTO(requestDTO);
            log.info("send email AUTHORIZE_CONTRACT contract to recipient {}", emailDTO);
            notificationService.sendEmailNotification(emailDTO);

            return recipientMapper.toDto(newRecipient);

        } catch (CustomException ce) {
            log.error("Error authorizeContract: {}", ce.getMessage());
            throw ce;
        } catch (Exception e) {
            log.error("Error --- authorizeContract: {}", e.getMessage());
            throw e;
        }
    }

}
