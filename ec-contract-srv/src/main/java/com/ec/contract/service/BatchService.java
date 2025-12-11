package com.ec.contract.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchService {

    private final NotificationService notificationService;
    private final ContractService contractService;

    @Scheduled(cron = "0 */5 * * * *")
    public void expireContractsDaily() {
        try {
            contractService.expireContractDaily();
        } catch (Exception e) {
            log.error("error expire contract {}" , e.getMessage());
        }
    }
}
