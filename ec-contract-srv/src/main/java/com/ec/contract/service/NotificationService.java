package com.ec.contract.service;

import com.ec.contract.model.dto.OrganizationDTO;
import com.ec.contract.model.dto.SendEmailDTO;
import com.ec.contract.model.dto.request.SendEmailRequestDTO;
import com.ec.contract.model.entity.Contract;
import com.ec.contract.model.entity.Customer;
import com.ec.contract.model.entity.Recipient;
import com.ec.contract.repository.ContractRepository;
import com.ec.contract.repository.RecipientRepository;
import com.ec.library.constants.ServiceEndpoints;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ContractRepository contractRepository;

    private final String API_URL = ServiceEndpoints.NOTICE_API;
    @Autowired
    private RecipientRepository recipientRepository;
    @Autowired
    private CustomerService customerService;

    public void sendEmailNotification(SendEmailDTO request) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<SendEmailDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                API_URL + "/internal/send-email",
                HttpMethod.POST,
                entity,
                Void.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Sending email notification");
        } else {
            log.error("Failed to send email notification");
        }
    }

    public SendEmailDTO setSendEmailDTO(SendEmailRequestDTO requestDTO){

        Contract contract = contractRepository.findById(requestDTO.getContractId())
                .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

        Recipient recipient = recipientRepository.findById(requestDTO.getRecipientId())
                .orElseThrow(() -> new CustomException(ResponseCode.RECIPIENT_NOT_FOUND));

        Customer customer = customerService.getCustomerById(contract.getCustomerId());
        OrganizationDTO organizationDTO = customerService.getOrganizationById(contract.getOrganizationId());

        String nameSender = customer.getName() + " - " + organizationDTO.getName();

        return SendEmailDTO.builder()
                .subject(requestDTO.getSubject())
                .recipient(recipient.getEmail())
                .code(requestDTO.getCode())
                .contractName(contract.getName())
                .contractNo(contract.getContractNo())
                .nameRecipient(recipient.getName())
                .nameSender(nameSender)
                .note(contract.getNote())
                .build();
    }

}
