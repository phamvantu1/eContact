package com.ec.contract.service;

import com.ec.contract.constant.ContractStatus;
import com.ec.contract.constant.RecipientRole;
import com.ec.contract.constant.RecipientStatus;
import com.ec.contract.model.dto.*;
import com.ec.contract.model.dto.request.SendEmailRequestDTO;
import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.library.constants.CommonConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class BpmnService {

    private final ContractService contractService;
    private final NotificationService notificationService;

    private RecipientService recipientService; // không final

    @Autowired
    public void setRecipientService(@Lazy RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    public void startContract(ContractResponseDTO contractResponseDTO) {

        log.info("-------Start process for contract: {}", contractResponseDTO.getId());

        ContractResponseDTO contractDto = null;

        try {

            contractDto = contractResponseDTO;

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
                        changeStatusAndNoticeToRecipient(recipientDto, contractDto.getId());

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
                        changeStatusAndNoticeToRecipient(recipientDto, contractDto.getId());

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
                                changeStatusAndNoticeToRecipient(recipientDto, contractDto.getId());

                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in startContract for contract {} : ", contractDto != null ? contractDto.getId() : "N/A");
        }
    }

    public void signContract(ContractResponseDTO contractDto, int recipientId) {

        log.info("=====start bpmn sign contract======");

        try {
            // xu ly nghiep vu
            processSignContract(contractDto, recipientId);

            // Kiem tra nguoi ky cuoi cung
        } catch (Exception e) {
            log.error("error", e);
        }
    }

    private String processSignContract(ContractResponseDTO contractDto, int recipientId) {

        String error = null;

        // Nguoi dang thuc hien
        var currentRecipient = getCurrentRecipient(contractDto, recipientId);
        var currentParticipant = getCurrentParticipant(contractDto, recipientId);

        // co nguoi ky cung thu tu trong cung to chuc chua xu ly thi dung
        for (var recipientDto : currentParticipant.getRecipients()) {
            if (recipientDto.getId() != recipientId
                    && recipientDto.getRole() == RecipientRole.SIGNER.getDbVal()
                    && recipientDto.getOrdering() == currentRecipient.getOrdering()
                    && recipientDto.getStatus() == 1) {
                return null;
            }
        }

        // kiem tra signer da xu ly het chua  , true la hết rồi
        boolean signerIsProcessed = checkSignerIsProcessed(contractDto, currentParticipant);

        try {
            RecipientDTO nextRecipientDto = null;
            if (signerIsProcessed) {
                log.info("[processSignContract][contract-{}] chuyen van thu", contractDto.getId());

                singerToArchiver(contractDto, currentParticipant);

            } else {
                log.info("[processSignContract][contract-{}] find other signer", contractDto.getId());

                // Tìm người ký còn lại của tổ chức
                int prevOrder = -1;
                for (var recipientDto : currentParticipant.getRecipients()) {
                    if (recipientDto.getRole() == RecipientRole.SIGNER.getDbVal().intValue()
                            && recipientDto.getStatus() == 0) {

                        if (prevOrder != -1 && prevOrder != recipientDto.getOrdering()) {
                            break;
                        }
                        // cap nhat trang thai dang xu ly
                        changeStatusAndNoticeToRecipient(recipientDto, contractDto.getId());

                        prevOrder = recipientDto.getOrdering();

                        nextRecipientDto = recipientDto;

                    }

                }
                if (nextRecipientDto != null) {
//                    sendNoticeConfigTransfer(contractDto, dto, customerDto, organizationDto, approveType, recipientId, nextRecipientDto);
                }

            }
            return null;
        } catch (Exception e) {

            log.error("error", e);
            error = ExceptionUtils.getFullStackTrace(e);
        } finally {
//             dong y

            checkFinish(contractDto);

//            sendStatusRecipient(String.valueOf(WebhookType.GET_RECIPIENT_STATUS),contractDto.getId(), contractDto.getOrganizationId());
        }

        return error;
    }

    private void singerToArchiver(ContractResponseDTO contractDto, ParticipantDTO currentParticipant) {

        int minOrder = -1;
        List<RecipientDTO> recipients = new ArrayList<>();

        for (var participantDto : contractDto.getParticipants()) {
            if (participantDto.getOrdering() > currentParticipant.getOrdering()) {
                break;
            }

            for (var recipientDto : participantDto.getRecipients()) {
                if (recipientDto.getRole() == RecipientRole.ARCHIVER.getDbVal()
                        && recipientDto.getOrdering() == 1
                        && recipientDto.getStatus() == 0) {

                    if (minOrder == -1 || participantDto.getOrdering() < minOrder) {
                        minOrder = participantDto.getOrdering();
                    }

                    if (participantDto.getOrdering() == minOrder) {
                        recipientDto.setParticipant(participantDto);
                        recipients.add(recipientDto);
                    }
                }
            }
        }

        for (var recipientDto : recipients) {
            changeStatusAndNoticeToRecipient(recipientDto, contractDto.getId());
        }

        if (recipients.size() > 0) {

            log.info("[SingerToArchiver] sms/email config email sms config");

            return;
        }

        log.info("khong co van thu chuyen luong xu ly sang to chuc tiep theo");
        switchToNextParticipant(contractDto, currentParticipant);
    }

    private void changeStatusAndNoticeToRecipient(RecipientDTO recipientDto, Integer contractId) {

        // cap nhat trang thai dang xu ly
        recipientDto.setStatus(RecipientStatus.PROCESSING.getDbVal());
        recipientService.changeRecipientProcessing(recipientDto.getId());

        SendEmailRequestDTO requestDTO = SendEmailRequestDTO.builder()
                .contractId(contractId)
                .recipientId(recipientDto.getId())
                .code(CommonConstants.CodeEmail.EMAIL)
                .build();

        switch (recipientDto.getRole()) {
            case 1 -> {
                requestDTO.setSubject(CommonConstants.SubjectEmail.COORDINATOR);
                requestDTO.setActionButton(CommonConstants.ActionButton.VIEW_CONTRACT);
                requestDTO.setTitleEmail(CommonConstants.TitleEmail.COORDINATOR);
                requestDTO.setUrl(CommonConstants.url.COORDINATOR);
            }
            case 2 -> {
                requestDTO.setSubject(CommonConstants.SubjectEmail.REVIEWER);
                requestDTO.setActionButton(CommonConstants.ActionButton.VIEW_CONTRACT);
                requestDTO.setTitleEmail(CommonConstants.TitleEmail.REVIEWER);
                requestDTO.setUrl(CommonConstants.url.REVIEWER);
            }
            case 3 -> {
                requestDTO.setSubject(CommonConstants.SubjectEmail.SIGNER);
                requestDTO.setActionButton(CommonConstants.ActionButton.VIEW_CONTRACT);
                requestDTO.setTitleEmail(CommonConstants.TitleEmail.SIGNER);
                requestDTO.setUrl(CommonConstants.url.SIGNER);
            }
            case 4 -> {
                requestDTO.setSubject(CommonConstants.SubjectEmail.ARCHIVER);
                requestDTO.setActionButton(CommonConstants.ActionButton.VIEW_CONTRACT);
                requestDTO.setTitleEmail(CommonConstants.TitleEmail.ARCHIVER);
                requestDTO.setUrl(CommonConstants.url.ARCHIVER);
            }
        }
        ;

        SendEmailDTO emailDTO = notificationService.setSendEmailDTO(requestDTO);
        log.info("send email ---- contract to recipient {}", emailDTO);
        notificationService.sendEmailNotification(emailDTO);

    }

    private void switchToNextParticipant(ContractResponseDTO contractDto, ParticipantDTO currentParticipant) {

        int minOrder = -1;
        for (var participantDto : contractDto.getParticipants()) {
            if (participantDto.getOrdering() > currentParticipant.getOrdering()) {
                minOrder = participantDto.getOrdering();
                break;
            }
        }

        boolean findReviewer = false;
        for (var participantDto : contractDto.getParticipants()) {
            if (participantDto.getOrdering() == minOrder) {
                for (var recipientDto : participantDto.getRecipients()) {
                    if (Objects.equals(recipientDto.getRole(), RecipientRole.REVIEWER.getDbVal())
                            && recipientDto.getOrdering() == 1) {
                        changeStatusAndNoticeToRecipient(recipientDto, contractDto.getId());

                        findReviewer = true;
                    }
                }
            }
        }

        if (findReviewer) {
            return;
        }

        // khong co nguoi xem xet chuyen den nguoi ky
        for (var participantDto : contractDto.getParticipants()) {
            if (participantDto.getOrdering() == minOrder) {
                for (var recipientDto : participantDto.getRecipients()) {
                    if (recipientDto.getRole() == RecipientRole.SIGNER.getDbVal()
                            && recipientDto.getOrdering() == 1) {

                        changeStatusAndNoticeToRecipient(recipientDto, contractDto.getId());

                    }
                }
            }
        }

    }

    /**
     * Kiem tra tat ca nguoi signer da xu ly hay chua
     *
     * @param contractDto
     * @param currentParticipant
     * @return
     */
    protected boolean checkSignerIsProcessed(ContractResponseDTO contractDto, ParticipantDTO currentParticipant) {

        // To chuc cua nguoi dang xu ly
        for (RecipientDTO recipientDto : currentParticipant.getRecipients()) {

            // Con bat ky recipient nao role = SIGN chua xu ly
            if (recipientDto.getRole() == RecipientRole.SIGNER.getDbVal().intValue() && recipientDto.getProcessAt() == null) {
                return false;
            }
        }

        // Cac to chuc khac co cung thu tu xu ly
        for (ParticipantDTO participantDto : contractDto.getParticipants()) {
            if (participantDto.getOrdering() == currentParticipant.getOrdering()) {

                for (RecipientDTO recipientDto : participantDto.getRecipients()) {

                    // Con bat ky recipient nao role < SIGN chua xu ly
                    if (recipientDto.getRole() == RecipientRole.SIGNER.getDbVal().intValue() && recipientDto.getProcessAt() == null) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void cancelContract(ContractResponseDTO contractDto) {

        try {

            log.info("contract.get: {}", contractDto);

        } catch (Exception e) {
            log.error("error", e);

        }
    }

    /**
     * Tu choi HD
     */
    public void rejectContract(ContractResponseDTO contractDto, ContractChangeStatusRequest request) {

        log.info("========start reject contract bpmn========");

        try {

            // Cap nhat trang thai hop dong thanh REJECT
            ContractResponseDTO changeStatusResponse = contractService.changeStatus(contractDto.getId(), ContractStatus.REJECTED.getDbVal(), request).get();

            log.info("Reject contract: " + changeStatusResponse);

//            // gui thong bao den tat ca nguoi tham gia HD
            for (ParticipantDTO participant : contractDto.getParticipants()) {
                for (RecipientDTO recipientDto : participant.getRecipients()) {

                    // send notice
                    SendEmailRequestDTO requestDTO = SendEmailRequestDTO.builder()
                            .subject(CommonConstants.SubjectEmail.REJECTED_CONTRACT)
                            .contractId(contractDto.getId())
                            .recipientId(recipientDto.getId())
                            .code(CommonConstants.CodeEmail.EMAIL)
                            .actionButton(CommonConstants.ActionButton.VIEW_CONTRACT)
                            .titleEmail(CommonConstants.TitleEmail.REJECTED_CONTRACT)
                            .url(CommonConstants.url.REJECTED_CONTRACT)
                            .build();
                    SendEmailDTO emailDTO = notificationService.setSendEmailDTO(requestDTO);
                    log.info("send email REJECTED_CONTRACT contract to recipient {}", emailDTO);
                    notificationService.sendEmailNotification(emailDTO);
                }

            }
        } catch (Exception e) {
            log.error("error", e);

        }
    }

    public void handleCoordinatorService(ContractResponseDTO contractDto, Integer recipientId) {

        try {
            log.info("start handleCoordinatorService for contract: {}", contractDto.getId());
            // xu ly nghiep vu
            processCoordinatorContract(contractDto, recipientId);
        } catch (Exception e) {
            log.error("error", e);

        }
    }

    private void processCoordinatorContract(ContractResponseDTO contractDto, int recipientId) {

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

        log.info("start check finish-===============");

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

    private boolean checkSignFinish(ContractResponseDTO contractDto) {
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

        log.info("this code test kkk tyu67");
        // check nguoi dieu phoi hop dong
        int minOrder = -1;

        var participants = contractDto.getParticipants();

        for (var participant : participants) {

            if (minOrder > -1 && participant.getOrdering() > minOrder) {
                log.info("found next coordinator, stop processing");
                return;
            }

            //TODO phamtu thứ tự xử lý của các tổ chức
            if (participant.getOrdering() > currentParticipant.getOrdering()) {
                for (var recipient : participant.getRecipients()) {
                    if (Objects.equals(recipient.getRole(), RecipientRole.COORDINATOR.getDbVal())) {

                        changeStatusAndNoticeToRecipient(recipient, contractDto.getId());

                        minOrder = participant.getOrdering();
                    }
                }
            }
        }

        if (minOrder > -1) {
            log.info("found next coordinator, stop processing");
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

                    changeStatusAndNoticeToRecipient(recipient, contractDto.getId());

                    findReviewr = true;

                }
            }
        }

        if (findReviewr) {
            log.info("kkkkkk ssss this review ");
            return;
        }

        // khong co nguoi xem xet chuyen den nguoi ky
        for (var participant : participants) {

            log.info("phamtu this code00000");

            if (participant.getOrdering() > minOrder) {
                break;
            }

            for (var recipient : participant.getRecipients()) {
                if (Objects.equals(recipient.getRole(), RecipientRole.SIGNER.getDbVal())
                        && (recipient.getOrdering() == minOrderingSigner)) {

                    changeStatusAndNoticeToRecipient(recipient, contractDto.getId());

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

        log.info("----participant of contract {}", contractDto.getParticipants());

        for (ParticipantDTO participant : contractDto.getParticipants()) {

            log.info("this --- participant of contract {}", contractDto.getParticipants());

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

        log.info("-----start --- [processReviewContract][contract-{}] recipient-{}", contractDto.getId(), recipientId);

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


            for (RecipientDTO recipientDto : recipients) {
                log.info("this code here check code he he participant id {}", recipientDto.getParticipant().getId());
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
                    log.info("this code is here check code recipient");
                    // la nguoi ky
                    if (recipientDto.getRole() == RecipientRole.SIGNER.getDbVal().intValue()) {
                        if (reviewerIsProcessed) {
                            log.info("pham tu log this here [processReviewContract][contract-{}] xong qua trinh xem xet chuyen ky", contractDto.getId());
                            reviewerToSigner(contractDto);
                            return;
                        }
                    } else if (recipientDto.getRole() == RecipientRole.REVIEWER.getDbVal() // chuyen den nguoi xem xet tiep cua cung to chuc
                            && recipientDto.getParticipant().getId() == currentParticipant.getId()) {

                        log.info("phamtu this code [processReviewContract][contract-{}] find other reviewer of participant-{} ", contractDto.getId(), currentParticipant.getId());
                        if (prevOrder != -1 && prevOrder != recipientDto.getOrdering()) {
                            break;
                        }

                        changeStatusAndNoticeToRecipient(recipientDto, contractDto.getId());
                    }
                    prevOrder = recipientDto.getOrdering();
                } else if (recipientDto.getId() == recipientId) {
                    log.info("check find is true");
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
                        changeStatusAndNoticeToRecipient(recipientDto, contractDto.getId());
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

//        // Cac to chuc khac co cung thu tu xu ly
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
