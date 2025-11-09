package com.ec.contract.service;

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

}
