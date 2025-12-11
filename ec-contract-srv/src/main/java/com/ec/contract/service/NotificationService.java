package com.ec.contract.service;

import com.ec.contract.model.dto.SendEmailDTO;
import com.ec.library.constants.ServiceEndpoints;
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

    private final String API_URL = ServiceEndpoints.NOTICE_API;

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

}
