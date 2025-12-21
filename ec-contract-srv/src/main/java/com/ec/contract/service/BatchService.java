package com.ec.contract.service;

import com.ec.contract.constant.ContractStatus;
import com.ec.contract.model.dto.SendEmailDTO;
import com.ec.contract.model.dto.request.SendEmailRequestDTO;
import com.ec.contract.model.entity.Contract;
import com.ec.contract.model.entity.Participant;
import com.ec.contract.model.entity.Recipient;
import com.ec.contract.repository.ContractRepository;
import com.ec.contract.repository.ParticipantRepository;
import com.ec.library.constants.CommonConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchService {

    private final NotificationService notificationService;
    private final ContractService contractService;
    private final ParticipantRepository participantRepository;
    private final ContractRepository contractRepository;

    @Scheduled(cron = "0 */1 * * * *")
    public void expireContractsDaily() {
        try {
            log.info("start run batch expire contract");

            String titleEmail = CommonConstants.TitleEmail.VIEW_CONTRACT
                    .replace("{status}", ContractStatus.EXPIRE.getViLabel());

            List<Contract> listContract = contractService.expireContractDaily();

            listContract.forEach(contract -> {

                Collection<Participant> participants = participantRepository.findByContractIdOrderByOrderingAsc(contract.getId());

                participants.forEach(participant -> {

                    Set<Recipient> recipientSet = participant.getRecipients();

                    recipientSet.forEach(recipient -> {

                        SendEmailRequestDTO requestDTO = SendEmailRequestDTO.builder()
                                .subject(CommonConstants.SubjectEmail.EXPIRED_CONTRACT)
                                .contractId(contract.getId())
                                .recipientId(recipient.getId())
                                .code(CommonConstants.CodeEmail.EMAIL)
                                .actionButton(CommonConstants.ActionButton.VIEW_CONTRACT)
                                .titleEmail(titleEmail)
                                .url(CommonConstants.url.VIEW_CONTRACT)
                                .build();

                        SendEmailDTO emailDTO = notificationService.setSendEmailDTO(requestDTO);

                        log.info("send email expire contract to recipient {}", emailDTO);

                        notificationService.sendEmailNotification(emailDTO);
                    });
                });
            });

        } catch (Exception e) {
            log.error("error expire contract {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void sendNoticeAboutExpireContracts() {
        try {
            log.info("start run batch send notice about expire contract");

            String titleEmail = CommonConstants.TitleEmail.VIEW_CONTRACT
                    .replace("{status}", ContractStatus.ABOUT_EXPIRE.getViLabel());

            List<Contract> listContractAboutToExpire = contractRepository.getContractsAboutToExpire();

            listContractAboutToExpire.forEach(contract -> {

                Collection<Participant> participants = participantRepository.findByContractIdOrderByOrderingAsc(contract.getId());

                participants.forEach(participant -> {

                    Set<Recipient> recipientSet = participant.getRecipients();

                    recipientSet.forEach(recipient -> {

                        SendEmailRequestDTO requestDTO = SendEmailRequestDTO.builder()
                                .subject(CommonConstants.SubjectEmail.EXPIRED_CONTRACT)
                                .contractId(contract.getId())
                                .recipientId(recipient.getId())
                                .code(CommonConstants.CodeEmail.EMAIL)
                                .actionButton(CommonConstants.ActionButton.VIEW_CONTRACT)
                                .titleEmail(titleEmail)
                                .url(CommonConstants.url.VIEW_CONTRACT)
                                .build();

                        SendEmailDTO emailDTO = notificationService.setSendEmailDTO(requestDTO);

                        log.info("send email ABOUT_EXPIRE contract to recipient {}", emailDTO);

                        notificationService.sendEmailNotification(emailDTO);
                    });
                });
            });

        } catch (Exception e) {
            log.error("error send notice about expire contract {}", e.getMessage());
        }
    }
}
