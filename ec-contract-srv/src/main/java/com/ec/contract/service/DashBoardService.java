package com.ec.contract.service;

import com.ec.contract.model.dto.response.DashBoardStatisticDTO;
import com.ec.contract.model.entity.Customer;
import com.ec.contract.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashBoardService {

    private final ContractRepository contractRepository;
    private final CustomerService customerService;

    public DashBoardStatisticDTO getMyProcessDashboard(Authentication authentication){
        try{

            String email = authentication.getName();

            int totalSigned = contractRepository.countTotalSignedContracts(email);
            int totalProcessing = contractRepository.countTotalProcessingContracts(email);
            int totalWaiting = contractRepository.countWaitingContracts(email);
            int totalAboutExpire = contractRepository.countAboutToExpireContracts(email);

            return DashBoardStatisticDTO.builder()
                    .totalSigned(totalSigned)
                    .totalProcessing(totalProcessing)
                    .totalWaiting(totalWaiting)
                    .totalAboutExpire(totalAboutExpire)
                    .build();

        }catch (Exception e){
            log.error("Error in getMyProcessDashboard: ", e);
            return null;
        }
    }

    public DashBoardStatisticDTO getMyContractDashboard(Authentication authentication, String fromDate, String toDate){
        try{

            String email = authentication.getName();

            Customer customer = customerService.getCustomerByEmail(email);

            int totalSigned = contractRepository.countTotalSignedMyContracts(customer.getId(), fromDate, toDate);
            int totalProcessing = contractRepository.countTotalProcessingMyContracts(customer.getId(), fromDate, toDate);
            int totalRejected = contractRepository.countTotalRejectedMyContracts(customer.getId(), fromDate, toDate);
            int totalCancelled = contractRepository.countTotalCancelMyContracts(customer.getId(), fromDate, toDate);
            int totalExpired = contractRepository.countTotalExpiredMyContracts(customer.getId(), fromDate, toDate);

            return DashBoardStatisticDTO.builder()
                    .totalSigned(totalSigned)
                    .totalProcessing(totalProcessing)
                    .totalReject(totalRejected)
                    .totalCancel(totalCancelled)
                    .totalExpires(totalExpired)
                    .build();

        }catch (Exception e){
            log.error("Error in getMyContractDashboard: ", e);
            return null;
        }
    }

    public DashBoardStatisticDTO countContractByOrganization(String fromDate, String toDate, Integer organizationId){
        try{

            int totalSigned = contractRepository.countTotalSignedContractsByOrganization(organizationId, fromDate, toDate);
            int totalProcessing = contractRepository.countTotalProcessingContractsByOrganization(organizationId, fromDate, toDate);
            int totalRejected = contractRepository.countTotalRejectedContractsByOrganization(organizationId, fromDate, toDate);
            int totalCancelled = contractRepository.countTotalCancelledContractsByOrganization(organizationId, fromDate, toDate);
            int totalExpired = contractRepository.countTotalExpiredContractsByOrganization(organizationId, fromDate, toDate);

            return DashBoardStatisticDTO.builder()
                    .totalSigned(totalSigned)
                    .totalProcessing(totalProcessing)
                    .totalReject(totalRejected)
                    .totalCancel(totalCancelled)
                    .totalExpires(totalExpired)
                    .build();

        }catch (Exception e){
            log.error("Error in countContractByOrganization: ", e);
            return null;
        }
    }
}
