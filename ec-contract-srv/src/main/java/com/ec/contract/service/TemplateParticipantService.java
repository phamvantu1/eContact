package com.ec.contract.service;

import com.ec.contract.mapper.TemplateParticipantMapper;
import com.ec.contract.model.dto.ParticipantDTO;
import com.ec.contract.model.dto.RecipientDTO;
import com.ec.contract.model.entity.TemplateContract;
import com.ec.contract.model.entity.TemplateField;
import com.ec.contract.model.entity.TemplateParticipant;
import com.ec.contract.model.entity.TemplateRecipient;
import com.ec.contract.repository.TemplateContractRepository;
import com.ec.contract.repository.TemplateFieldRepository;
import com.ec.contract.repository.TemplateParticipantRepository;
import com.ec.contract.repository.TemplateRecipientRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateParticipantService {

    private final TemplateContractRepository templateContractRepository;
    private final TemplateParticipantRepository templateParticipantRepository;
    private final TemplateRecipientRepository templateRecipientRepository;
    private final TemplateFieldRepository templateFieldRepository;
    private final TemplateParticipantMapper templateParticipantMapper;

    @Transactional
    public List<ParticipantDTO> createParticipant(List<ParticipantDTO> participantDTOList,
                                                  Integer contractId) {
        try {

            log.info("start createParticipant for contractId: {}", contractId);

            TemplateContract contract = templateContractRepository.findById(contractId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            if (hasDuplicateRecipientEmail(participantDTOList)) {
                log.error("Duplicate recipient email found in participants for contractId: {}", contractId);
                throw new CustomException(ResponseCode.DUPLICATE_RECIPIENT_EMAIL);
            }

            // xoa cac doi tac bi trung
            participantDTOList = removeParticipantDuplicates(participantDTOList);

            var currentParticipants = templateParticipantRepository.findByContractIdOrderByOrderingAsc(contractId);

            // xu ly truong hop FE khong truyen participant id
            for (var participantDto : participantDTOList) {
                if (participantDto.getId() == null) {
                    var tmpRecipient = participantDto.getRecipients().stream()
                            .filter(r -> r.getId() != null)
                            .findAny()
                            .orElse(null);

                    if (tmpRecipient != null) {
                        var r = templateRecipientRepository.findById(tmpRecipient.getId()).orElse(null);
                        if (r != null) {
                            participantDto.setId(r.getParticipant().getId());
                        }
                    }
                }
            }

            for (var p : currentParticipants) {
                boolean exists = false;
                for (var participantDto : participantDTOList) {
                    if (participantDto.getId() != null &&
                            participantDto.getId().intValue() == p.getId().intValue()) {

                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    templateParticipantRepository.deleteById(p.getId());
                }
            }

            final Collection<TemplateParticipant> participantCollection = new ArrayList<>();

            for (var participantDto : participantDTOList) {
                TemplateParticipant participant;

                if (participantDto.getId() != null) {
                    participant = templateParticipantRepository.findById(participantDto.getId())
                            .orElseThrow(() -> new CustomException(ResponseCode.PARTICIPANT_NOT_FOUND));
                } else {
                    participant = new TemplateParticipant();
                }

                BeanUtils.copyProperties(participantDto, participant,
                        "type", "status", "recipients");

                // 🧠 Lấy danh sách recipient hiện tại
                Set<TemplateRecipient> existingRecipients = participant.getRecipients() != null
                        ? participant.getRecipients()
                        : new HashSet<>();

                Set<TemplateRecipient> updatedRecipients = new HashSet<>();

                for (var recipientDto : participantDto.getRecipients()) {
                    TemplateRecipient recipient;

                    if (recipientDto.getId() != null) {
                        // Tìm recipient cũ trong danh sách hiện tại
                        recipient = existingRecipients.stream()
                                .filter(r -> r.getId().equals(recipientDto.getId()))
                                .findFirst()
                                .orElse(new TemplateRecipient());
                    } else {
                        recipient = new TemplateRecipient();
                    }

                    BeanUtils.copyProperties(recipientDto, recipient,
                            "fields", "signType", "role", "status");

                    recipient.setSignType(recipientDto.getSignType());
                    recipient.setRole(recipientDto.getRole());
                    recipient.setStatus(recipientDto.getStatus());
                    recipient.setParticipant(participant);

                    updatedRecipients.add(recipient);
                }

                // orphanRemoval sẽ tự xóa những recipient cũ không còn trong updatedRecipients
                participant.getRecipients().clear();
                participant.getRecipients().addAll(updatedRecipients);

                participant.setContractId(contractId);
                participant.setType(participantDto.getType());
                participant.setStatus(participantDto.getStatus());

                participantCollection.add(participant);
            }

            final var participantList = templateParticipantRepository.saveAll(participantCollection);

            for(TemplateParticipant participant: participantList) {
                Set<TemplateRecipient> recipientSet = participant.getRecipients();

                for(TemplateRecipient recipient : recipientSet) {
                    Collection<TemplateField> fieldCollection = templateFieldRepository.findAllByRecipientId(recipient.getId());
                    for(TemplateField field : fieldCollection) {
                        recipient.addField(field);
                    }
                }
            }

            var result = templateParticipantMapper.toDtoList(participantList);

            sortRecipient(result);

            return result;

        } catch (CustomException e) {
            throw e;
        } catch (Exception ex) {
            log.error("Error creating participants for contractId {}: {}", contractId, ex.getMessage());
            throw ex;
        }
    }
    private boolean hasDuplicateRecipientEmail(List<ParticipantDTO> participantDTOList) {
        if (participantDTOList == null || participantDTOList.isEmpty()) {
            return false;
        }

        // Duyệt tất cả recipients trong tất cả participants
        return participantDTOList.stream()
                .filter(p -> p.getRecipients() != null)
                .flatMap(p -> p.getRecipients().stream())
                .map(RecipientDTO::getEmail)
                .filter(Objects::nonNull)
                .map(String::toLowerCase) // bỏ qua hoa/thường
                .collect(java.util.stream.Collectors.groupingBy(e -> e, java.util.stream.Collectors.counting()))
                .values()
                .stream()
                .anyMatch(count -> count > 1);
    }

    private List<ParticipantDTO> removeParticipantDuplicates(List<ParticipantDTO> participantDTOList) {
        List<ParticipantDTO> uniqueParticipants = new ArrayList<>();
        for (var p : participantDTOList) {
            boolean exists = false;
            for (var p2 : uniqueParticipants) {
                if (p.isSame(p2)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                uniqueParticipants.add(p);
            }
        }
        return uniqueParticipants;
    }

    // sắp xếp recipient theo role và ordering
    public void sortRecipient(Collection<ParticipantDTO> participants) {
        try {
            for (var participant : participants) {
                var recipients = participant.getRecipients().stream()
                        .sorted(Comparator.comparing(RecipientDTO::getRole).thenComparing(RecipientDTO::getOrdering))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                participant.setRecipients(recipients);
            }
        } catch (Exception e) {
            log.error("Lỗi sắp xếp recipient", e);
        }
    }


}
