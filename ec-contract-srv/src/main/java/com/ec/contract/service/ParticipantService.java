package com.ec.contract.service;

import com.ec.contract.mapper.ParticipantMapper;
import com.ec.contract.model.dto.ParticipantDTO;
import com.ec.contract.model.dto.RecipientDTO;
import com.ec.contract.model.entity.Contract;
import com.ec.contract.model.entity.Participant;
import com.ec.contract.model.entity.Recipient;
import com.ec.contract.repository.ContractRepository;
import com.ec.contract.repository.FieldRepository;
import com.ec.contract.repository.ParticipantRepository;
import com.ec.contract.repository.RecipientRepository;
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
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final ContractRepository contractRepository;
    private final RecipientRepository recipientRepository;
    private final ParticipantMapper participantMapper;
    private final FieldRepository fieldRepository;

    @Transactional
    public List<ParticipantDTO> createParticipant(List<ParticipantDTO> participantDTOList,
                                                  Integer contractId) {
        try {

            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            if (hasDuplicateRecipientEmail(participantDTOList)) {
                log.error("Duplicate recipient email found in participants for contractId: {}", contractId);
                throw new CustomException(ResponseCode.DUPLICATE_RECIPIENT_EMAIL);
            }

            // xoa cac doi tac bi trung
            participantDTOList = removeParticipantDuplicates(participantDTOList);

            var currentParticipants = participantRepository.findByContractIdOrderByOrderingAsc(contractId);

            // xu ly truong hop FE khong truyen participant id
            for (var participantDto : participantDTOList) {
                if (participantDto.getId() == null) {
                    var tmpRecipient = participantDto.getRecipients().stream()
                            .filter(r -> r.getId() != null)
                            .findAny()
                            .orElse(null);

                    if (tmpRecipient != null) {
                        var r = recipientRepository.findById(tmpRecipient.getId()).orElse(null);
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
                    participantRepository.deleteById(p.getId());
                }
            }

            final Collection<Participant> participantCollection = new ArrayList<>();

            for (var participantDto : participantDTOList) {
                Participant participant;

                if (participantDto.getId() != null) {
                    participant = participantRepository.findById(participantDto.getId())
                            .orElseThrow(() -> new CustomException(ResponseCode.PARTICIPANT_NOT_FOUND));
                } else {
                    participant = new Participant();
                }

                BeanUtils.copyProperties(participantDto, participant,
                        "type", "status", "recipients");

                // 🧠 Lấy danh sách recipient hiện tại
                Set<Recipient> existingRecipients = participant.getRecipients() != null
                        ? participant.getRecipients()
                        : new HashSet<>();

                Set<Recipient> updatedRecipients = new HashSet<>();

                for (var recipientDto : participantDto.getRecipients()) {
                    Recipient recipient;

                    if (recipientDto.getId() != null) {
                        // Tìm recipient cũ trong danh sách hiện tại
                        recipient = existingRecipients.stream()
                                .filter(r -> r.getId().equals(recipientDto.getId()))
                                .findFirst()
                                .orElse(new Recipient());
                    } else {
                        recipient = new Recipient();
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

            final var participantList = participantRepository.saveAll(participantCollection);

            var result = participantMapper.toDtoList(participantList);

            sortRecipient(result);

            return result;

        } catch (CustomException e) {
            throw e;
        } catch (Exception ex) {
            log.info("Error creating participants for contractId {}: {}", contractId, ex.getMessage());
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public ParticipantDTO getParticipantById(Integer participantId) {
        try{
            Participant participant = participantRepository.findById(participantId)
                    .orElseThrow(() -> new CustomException(ResponseCode.PARTICIPANT_NOT_FOUND));

            ParticipantDTO participantDTO = participantMapper.toDto(participant);

            sortRecipient(List.of(participantDTO));

            return participantDTO;
        }catch (CustomException e) {
            throw e;
        } catch (Exception ex) {
            log.info("Error getting participants for contractId {}: {}", participantId, ex.getMessage());
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<ParticipantDTO> getParticipantsByContractId(Integer contractId) {
        try{
            Collection<Participant> participantList = participantRepository.findByContractIdOrderByOrderingAsc(contractId);

            List<ParticipantDTO> participantDTOList = participantMapper.toDtoList((List<Participant>) participantList);

            sortRecipient(participantDTOList);

            return participantDTOList;
        }catch (CustomException e) {
            throw e;
        } catch (Exception ex) {
            log.info("Error getting participants for contractId {}: {}", contractId, ex.getMessage());
            throw ex;
        }
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

    private void sortParticipant(List<ParticipantDTO> participants) {
        try{
            participants.sort((p1, p2) -> {
                if (Objects.equals(p1.getType(), p2.getType())) {
                    return p1.getOrdering() - p2.getOrdering();
                }
                return p1.getType() - p2.getType();
            });
        }catch (Exception e){
            log.error("lỗi sắp xếp lại tổ chức trong hợp đồng", e);
        }
    }


}
