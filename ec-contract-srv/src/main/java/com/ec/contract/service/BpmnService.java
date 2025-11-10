package com.ec.contract.service;

import com.ec.contract.constant.ContractStatus;
import com.ec.contract.constant.RecipientRole;
import com.ec.contract.constant.RecipientStatus;
import com.ec.contract.model.dto.OrganizationDTO;
import com.ec.contract.model.dto.ParticipantDTO;
import com.ec.contract.model.dto.RecipientDTO;
import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.contract.model.entity.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

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
            log.info("start handleCoordinatorService for contract: {}", contractDto.getId());
            // xu ly nghiep vu
            processCoordinatorContract(contractDto, recipientId);
        } catch (Exception e) {
            log.error("error", e);

        }
    }

    private void processCoordinatorContract(ContractResponseDTO contractDto,  int recipientId) {

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

    public void reviewContract(ContractResponseDTO contractDto, Integer recipientId) {

        try {
            log.info("Contract review : {}", contractDto);
            // xu ly nghiep vu
             processReviewContract(contractDto, recipientId);
        } catch (Exception e) {
            log.error("error", e);
        }
    }

    private void processReviewContract(ContractResponseDTO contractDto, int recipientId) {

        log.info("[processReviewContract][contract-{}] recipient-{}", contractDto.getId(), recipientId);

        // Nguoi dang thuc hien
        RecipientDTO currentRecipient = getCurrentRecipient(contractDto, recipientId);
        ParticipantDTO currentParticipant = getCurrentParticipant(contractDto, recipientId);

        try {

            boolean find = false;

            // true là hết rồi , false là còn xem xét
            boolean reviewerIsProcessed = checkReviewerIsProcessed(contractDto, currentParticipant);

            List<RecipientDTO> recipients = new ArrayList<>();

            for (var participant : contractDto.getParticipants()) {
                for (var recipient : participant.getRecipients()) {
                    recipient.setParticipant(participant);
                    recipients.add(recipient);
                }
            }
            contractService.sortParallel(contractDto, recipients);

            int prevOrder = -1;
            RecipientDTO nextRecipientDto = null;

            for (RecipientDTO recipientDto : recipients) {
                // con nguoi xem xet cung thu tu voi nguoi xem xet trong cung to chuc chua xu ly thi dung
                if (recipientDto.getId() != recipientId
                        && recipientDto.getRole() == RecipientRole.REVIEWER.getDbVal()
                        && recipientDto.getParticipant().equals(currentParticipant)
                        && recipientDto.getOrdering() == currentRecipient.getOrdering()
                        && recipientDto.getStatus() == 1
                ) {
                    log.info("recipient-{} haven't processed yet", recipientId);
                    return;
                }

                // TODO: check them truong hop gui thong tin nguoi xu ly cua doi tac tiep theo
                if (find && recipientDto.getId() != recipientId && recipientDto.getStatus() == 0) {
                    // la nguoi ky
                    if (recipientDto.getRole() == RecipientRole.SIGNER.getDbVal().intValue()) {
                        if (reviewerIsProcessed) {
                            log.info("[processReviewContract][contract-{}] xong qua trinh xem xet chuyen ky",  contractDto.getId());
                            reviewerToSigner(contractDto);
                            return;
                        }
                    } else if (recipientDto.getRole() == RecipientRole.REVIEWER.getDbVal() // chuyen den nguoi xem xet tiep cua cung to chuc
                            && recipientDto.getParticipant().getId() == currentParticipant.getId()) {

                        log.info("[processReviewContract][contract-{}] find other reviewer of participant-{} ",  contractDto.getId(), currentParticipant.getId());
                        if (prevOrder != -1 && prevOrder != recipientDto.getOrdering()) {
                            break;
                        }

                        recipientDto.setStatus(RecipientStatus.PROCESSING.getDbVal());
                        recipientService.changeRecipientProcessing(recipientDto.getId());
                    }
                    prevOrder = recipientDto.getOrdering();
                } else if (recipientDto.getId() == recipientId) {
                    find = true;
                }

            } // end loop recipients

            // TODO: Kiem tra da hoan thanh ky HD
        } catch (Exception e) {

            log.error("error", e);

        } finally {
            // dong y
            checkFinish(contractDto);

        }

    }

    /**
     * Cap nhat trang thai cua tat ca Signer
     *
     * @param contractDto
     */
    private void reviewerToSigner(ContractResponseDTO contractDto) {

        int minOrder = -1;
        for (var participantDto : contractDto.getParticipants()) {
            for (var recipientDto : participantDto.getRecipients()) {
                if (recipientDto.getRole() == RecipientRole.SIGNER.getDbVal()
                        && recipientDto.getOrdering() == 1
                        && recipientDto.getStatus() == 0
                ) {
                    if (minOrder == -1 || participantDto.getOrdering() < minOrder) {
                        minOrder = participantDto.getOrdering();
                    }

                    if (participantDto.getOrdering() == minOrder) {
                        recipientDto.setStatus(RecipientStatus.PROCESSING.getDbVal());
                        recipientService.changeRecipientProcessing(recipientDto.getId());
                    }
                }
            }
        }
    }


    /**
     * Kiem tra co to chuc nao cung thu tu xu ly khong
     *
     * @param contractDto
     * @param currentParticipant
     * @return
     */
    protected boolean participantEqualOrder(ContractResponseDTO contractDto, ParticipantDTO currentParticipant) {

        for (ParticipantDTO participantDto : contractDto.getParticipants()) {

            // co to chuc khac co cung thu tu Ordering
            if (participantDto.getOrdering() == currentParticipant.getOrdering() && participantDto.getId() != currentParticipant.getId()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Kiem tra tat ca nguoi review da xu ly hay chua
     *
     * @param contractDto
     * @param currentParticipant
     * @return
     */
    protected boolean checkReviewerIsProcessed(ContractResponseDTO contractDto, ParticipantDTO currentParticipant) {

        // To chuc cua nguoi dang xu ly
        for (RecipientDTO recipientDto : currentParticipant.getRecipients()) {

            // Con bat ky recipient nao role < SIGN chua xu ly
            if (recipientDto.getRole() < RecipientRole.SIGNER.getDbVal() && recipientDto.getProcessAt() == null) {
                return false;
            }
        }

        // Cac to chuc khac co cung thu tu xu ly
        for (ParticipantDTO participantDto : contractDto.getParticipants()) {
            if (Objects.equals(participantDto.getOrdering(), currentParticipant.getOrdering())) {

                for (RecipientDTO recipientDto : participantDto.getRecipients()) {

                    // Con bat ky recipient nao role < SIGN chua xu ly
                    if (recipientDto.getRole() < RecipientRole.SIGNER.getDbVal() && recipientDto.getProcessAt() == null) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

}
