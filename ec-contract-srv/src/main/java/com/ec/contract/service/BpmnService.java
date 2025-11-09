package com.ec.contract.service;

import com.ec.contract.constant.ContractStatus;
import com.ec.contract.constant.ParticipantType;
import com.ec.contract.constant.RecipientRole;
import com.ec.contract.constant.RecipientStatus;
import com.ec.contract.model.dto.OrganizationDTO;
import com.ec.contract.model.dto.ParticipantDTO;
import com.ec.contract.model.dto.RecipientDTO;
import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.contract.model.entity.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BpmnService {

    private final CustomerService customerService;
    private final RecipientService recipientService;
    private final ContractService contractService;

    public void startContract(ContractResponseDTO contractResponseDTO) {

        ContractResponseDTO contractDto = null;

        try {

            contractDto = contractResponseDTO;

            if (contractDto == null) {
                return;
            }

            // Lay thong tin customer tao HD
            Customer customer = customerService.getCustomerById(contractDto.getCreatedBy());

            OrganizationDTO organizationDTO = customerService.getOrganizationById(contractDto.getOrganizationId());

            boolean findCoordinator = false;
            int minOrder = -1;

            // Gửi cho người điều phối trước
            for (ParticipantDTO participant : contractDto.getParticipants()) {
                if (minOrder > -1 && participant.getOrdering() > minOrder) {
                    return;
                }

                Set<RecipientDTO> recipients = participant.getRecipients();
                for (RecipientDTO recipientDto : recipients) {
                    if (Objects.equals(recipientDto.getRole(), RecipientRole.COORDINATOR.getDbVal()) && recipientDto.getStatus() == 0) {

                        if (minOrder == -1) {
                            minOrder = participant.getOrdering();
                        }

                        // cap nhat trang thai dang xu ly
                        recipientDto.setStatus(RecipientStatus.PROCESSING.getDbVal());
                        recipientService.changeRecipientProcessing(recipientDto.getId());

                        findCoordinator = true;
                    }
                }
            }

            if (findCoordinator) {
                return;
            }

            List<Integer> recipientSigner = new ArrayList<Integer>();
            boolean findRecipient = false;

            // start luong ky HD
            int signOrdering = 0;

            for (ParticipantDTO participant : contractDto.getParticipants()) {
                // participant khong cung Ordering voi participant truoc do
                if (signOrdering != 0 && signOrdering != participant.getOrdering())
                    break;

                signOrdering = participant.getOrdering();

                Set<RecipientDTO> recipients = participant.getRecipients();

                // TODO kiem tra recipients

                for (RecipientDTO recipientDto : recipients) {

                    // TODO: check cac recipient cung ordering
                    if (recipientDto.getRole() == RecipientRole.REVIEWER.getDbVal().intValue()
                            && recipientDto.getOrdering() == 1) {

                        // cap nhat trang thai dang xu ly
                        recipientDto.setStatus(RecipientStatus.PROCESSING.getDbVal());
                        recipientService.changeRecipientProcessing(recipientDto.getId());

                        // da tim duoc nguoi xu ly
                        findRecipient = true;

                        // remove recipientSigner
                        if (!recipientSigner.isEmpty()) {
                            recipientSigner.clear();
                            log.info("Cleared recipientSigner list");
                        }

                    } else if (recipientDto.getRole().equals(RecipientRole.SIGNER.getDbVal())
                            && recipientDto.getOrdering() == 1) {
                        // add danh sach nguoi ky cho xu ly
                        if (!findRecipient) {

                            log.info("[contract-{}] add {} into recipientSigner", contractDto.getId(), recipientDto.getId());

                            recipientSigner.add(recipientDto.getId());
                        }
                    }
                } // end for participants
            }

            // Truong hop khong co bat ky nguoi xu ly nao truoc SIGNER
            if (!recipientSigner.isEmpty()) {

                log.info("[contract-{}] notify SIGNER: ", contractDto.getId());

                for (Integer recipientId : recipientSigner) {

                    for (ParticipantDTO participant : contractDto.getParticipants()) {

                        Set<RecipientDTO> recipients = participant.getRecipients();
                        for (RecipientDTO recipientDto : recipients) {

                            if (recipientDto.getId() == recipientId.intValue()) {

                                // cap nhat trang thai dang xu ly
                                recipientDto.setStatus(RecipientStatus.PROCESSING.getDbVal());

                                recipientService.changeRecipientProcessing(recipientDto.getId());

                                log.info("[contract-{}] notify SIGNER: {}", contractDto.getId(), recipientDto.getId());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in startContract for contract {} : ", contractDto != null ? contractDto.getId() : "N/A");
        }
    }

    public void cancelContract(ContractResponseDTO contractDto) {

        try {

            log.info("contract.get: {}", contractDto);

        } catch (Exception e) {
            log.error("error", e);

        }
    }

    public void handleCoordinatorService(ContractResponseDTO contractDto, Integer recipientId ) {

        try {
            // xu ly nghiep vu
            processCoordinatorContract(contractDto, recipientId);
        } catch (Exception e) {
            log.error("error", e);

        }
    }

    private void processCoordinatorContract(ContractResponseDTO contractDto,  int recipientId) {

        // Lay thong tin customer tao HD
        var customerDto = customerService.getCustomerById(contractDto.getCustomerId());
        var organizationDto = customerService.getCustomerByOrganizationId(contractDto.getOrganizationId());

        // to chuc dang thuc hien
        var currentParticipant = getCurrentParticipant(contractDto, recipientId);

        try {
            coordinatorToNext(contractDto, currentParticipant);
        } catch (Exception e) {
            log.error("error when processCoordinatorContract : ", e);

        } finally {
                checkFinish(contractDto);
        }
    }

    /**
     * Kiem tra hoan thanh luong ky HD
     *
     * @param contractDto
     */
    private void checkFinish(ContractResponseDTO contractDto) {

        boolean finish = true;

        try {
            // Kiem tra hoan thanh luong ky HD - Co the su dung get last item cua recipient
            for (ParticipantDTO participantDto : contractDto.getParticipants()) {
                for (RecipientDTO recipientDto : participantDto.getRecipients()) {
                    if (recipientDto.getStatus() != 2 && !recipientDto.getStatus().equals(RecipientStatus.AUTHORIZE.getDbVal())) {
                        finish = false;
                        return;
                    }
                }
            }
        } catch (Exception e) {
            finish = false;
            log.error("error", e);
        } finally {

            // Da hoan thanh luong HD
            if (finish) {
                try {
                    // 1. Cap nhat thong tin HD la hoan thanh ContractStatus.SIGNED
                    ContractResponseDTO res = contractService.changeStatus(contractDto.getId(), ContractStatus.SIGNED.getDbVal(), null).get();
                    log.info("Finish contract: " + res);
                } catch (Exception e) {
                    log.error("error", e);
                }
            }
        }
    }

    private boolean checkSignFinish(ContractResponseDTO contractDto){
        boolean finish = true;
        for (ParticipantDTO participantDto : contractDto.getParticipants()) {
            for (RecipientDTO recipientDto : participantDto.getRecipients()) {
                if (recipientDto.getStatus() != 2 && !recipientDto.getStatus().equals(RecipientStatus.AUTHORIZE.getDbVal())) {
                    return false;
                }
            }
        }
        return finish;
    }

    private void coordinatorToNext(ContractResponseDTO contractDto, ParticipantDTO currentParticipant) {
        // check nguoi dieu phoi hop dong
        int minOrder = -1;

        RecipientDTO recipientDto = null;
        var participants = contractDto.getParticipants();
        for (var participant : participants) {
            if (minOrder > -1 && participant.getOrdering() > minOrder) {
                return;
            }
            //TODO phamtu thứ tự xử lý của các tổ chức
            if (participant.getOrdering() > currentParticipant.getOrdering()) {
                for (var recipient : participant.getRecipients()) {
                    if (Objects.equals(recipient.getRole(), RecipientRole.COORDINATOR.getDbVal())) {

                        recipient.setStatus(RecipientStatus.PROCESSING.getDbVal());
                        recipientService.changeRecipientProcessing(recipient.getId());

                        minOrder = participant.getOrdering();
                        recipientDto = recipient;
                    }
                }
            }
        }

        if (minOrder > -1) {
            return;
        }

        // khong con nguoi dieu phoi nao chuyen luong xu ly ve to chuc co thu tu xu ly dau tien
        minOrder = contractDto.getParticipants().stream().findFirst().get().getOrdering();
        int minOrderingReviewer = 1;
        int minOrderingSigner = 1;

        // kiểm tra tổ chức số thứ tự nhỏ nhất có tổ chức xử lý song song hay không
        var participantsDefault = contractDto.getParticipants();

        int minOrderingParticipant = participantsDefault.stream().map(ParticipantDTO::getOrdering).min(Integer::compareTo).orElse(minOrder);

        var isOrganization = participantsDefault.stream().anyMatch(p -> p.getOrdering() == minOrderingParticipant && (Objects.equals(p.getType(), ParticipantType.MY_ORGANIZATION.getDbVal()) || Objects.equals(p.getType(), ParticipantType.ORGANIZATION.getDbVal())));

        var participantDuplicate = isOrganization && participantsDefault.stream().filter(p -> p.getOrdering() == minOrderingParticipant).count() > 1;

        if (participantDuplicate) {
            participants = participantsDefault;
            minOrder = minOrderingParticipant;
            var recipientsNextProcess = participants.stream().filter(p -> p.getOrdering() == minOrderingParticipant).flatMap(p -> p.getRecipients().stream()).collect(Collectors.toList());
            minOrderingReviewer = recipientsNextProcess.stream().filter(r -> Objects.equals(r.getRole(), RecipientRole.REVIEWER.getDbVal())).map(RecipientDTO::getOrdering).min(Integer::compareTo).orElse(minOrderingReviewer);
            minOrderingSigner = recipientsNextProcess.stream().filter(r -> Objects.equals(r.getRole(), RecipientRole.SIGNER.getDbVal())).map(RecipientDTO::getOrdering).min(Integer::compareTo).orElse(minOrderingSigner);
        }

        var hasMyOrganization = participantsDefault.stream().anyMatch(p -> p.getOrdering() == minOrderingParticipant && Objects.equals(p.getType(), ParticipantType.MY_ORGANIZATION.getDbVal()));

        var hasPartnerOrganization = participantsDefault.stream().anyMatch(p -> p.getOrdering() == minOrderingParticipant && Objects.equals(p.getType(), ParticipantType.ORGANIZATION.getDbVal()));

        var hasPersonal = participantsDefault.stream().anyMatch(p -> p.getOrdering() == minOrderingParticipant && Objects.equals(p.getType(), ParticipantType.PERSONAL.getDbVal()));
        // Kiểm tra điều kiện hợp lệ
        var participantDuplicate2 = hasMyOrganization && hasPersonal;

        if (participantDuplicate2) {
            log.info("Xử lý khi các tổ chức và cá nhân có cùng số thứ tự");
            participants = participantsDefault;
            minOrder = minOrderingParticipant;
            var recipientsNextProcess = participants.stream().filter(p -> p.getOrdering() == minOrderingParticipant).flatMap(p -> p.getRecipients().stream()).collect(Collectors.toList());
            minOrderingReviewer = recipientsNextProcess.stream().filter(r -> Objects.equals(r.getRole(), RecipientRole.REVIEWER.getDbVal())).map(RecipientDTO::getOrdering).min(Integer::compareTo).orElse(minOrderingReviewer);
            log.info("Xử lý khi các tổ chức và cá nhân có cùng số thứ tự minOrderingReviewer: "+ minOrderingReviewer);
            minOrderingSigner = recipientsNextProcess.stream()
                    .filter(Objects::nonNull) // Loại bỏ phần tử null
//                    .filter(r -> Objects.equals(r.getRole(), RecipientRole.SIGNER.getDbVal())
//                            && r.getParticipantId() != null // Kiểm tra participant không null
//                            && r.getParticipantId().getType() == ParticipantType.PERSONAL.getDbVal())
                    .map(r -> {
                        Integer ordering = r.getOrdering();
                        if (ordering == null) {
                            throw new NullPointerException("Ordering value is null for recipient: " + r);
                        }
                        return ordering;
                    })
                    .min(Integer::compareTo)
                    .orElse(minOrderingSigner);
            log.info("Xử lý khi các tổ chức và cá nhân có cùng số thứ tự minOrderingSigner: "+ minOrderingSigner);

        }
        boolean findReviewr = false;
        for (var participant : participants) {
            if (participant.getOrdering() > minOrder) {
                break;
            }

            for (var recipient : participant.getRecipients()) {
                if (Objects.equals(recipient.getRole(), RecipientRole.REVIEWER.getDbVal())
                        && recipient.getOrdering() == minOrderingReviewer) {

                    recipient.setStatus(RecipientStatus.PROCESSING.getDbVal());
                    recipientService.changeRecipientProcessing(recipient.getId());

                    findReviewr = true;
                    recipientDto = recipient;
                }
            }
        }

        if (findReviewr) {
            return;
        }

        // khong co nguoi xem xet chuyen den nguoi ky
        for (var participant : participants) {
            if (participant.getOrdering() > minOrder) {
                break;
            }

            for (var recipient : participant.getRecipients()) {
                if (Objects.equals(recipient.getRole(), RecipientRole.SIGNER.getDbVal())
                        && (recipient.getOrdering() == minOrderingSigner)) {

                    recipient.setStatus(RecipientStatus.PROCESSING.getDbVal());
                    recipientService.changeRecipientProcessing(recipient.getId());

                }
            }
        }
    }

    /**
     * Lay nguoi dang thuc hien xu ly HD
     *
     * @param contractDto
     * @return RecipientDto
     */
    public RecipientDTO getCurrentRecipient(ContractResponseDTO contractDto, int recipientId) {

        for (ParticipantDTO participant : contractDto.getParticipants()) {

            Set<RecipientDTO> recipients = participant.getRecipients();
            for (RecipientDTO recipientDto : recipients) {

                // la nguoi thuc hien
                if (recipientDto.getId() == recipientId) {

                    return recipientDto;
                }
            }
        }

        return null;
    }

    /**
     * Lay to chuc dang xu ly HD
     *
     * @param contractDto
     * @return RecipientDto
     */
    public ParticipantDTO getCurrentParticipant(ContractResponseDTO contractDto, int recipientId) {

        for (ParticipantDTO participant : contractDto.getParticipants()) {

            Set<RecipientDTO> recipients = participant.getRecipients();
            for (RecipientDTO recipientDto : recipients) {

                // la nguoi thuc hien
                if (recipientDto.getId() == recipientId) {

                    return participant;
                }
            }
        }

        return null;
    }

}
