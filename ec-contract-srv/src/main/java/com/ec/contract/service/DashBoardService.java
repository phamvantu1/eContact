package com.ec.contract.service;

import com.ec.contract.constant.ContractStatus;
import com.ec.contract.model.dto.response.ContractCustomerDTO;
import com.ec.contract.model.dto.response.DashBoardStatisticDTO;
import com.ec.contract.model.dto.response.StaticCustomerUseContract;
import com.ec.contract.model.entity.Customer;
import com.ec.contract.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashBoardService {

    private final ContractRepository contractRepository;
    private final CustomerService customerService;

    public DashBoardStatisticDTO getMyProcessDashboard(Authentication authentication) {
        try {

            String email = authentication.getName();

            int totalSigned = contractRepository.countMyProcessContract(email, ContractStatus.SIGNED.getDbVal());
            int totalProcessing = contractRepository.countMyProcessContract(email, ContractStatus.PROCESSING.getDbVal());
            int totalWaiting = contractRepository.countMyProcessContract(email, 50); // fix cung 50 la doi
            int totalAboutExpire = contractRepository.countMyProcessContract(email, ContractStatus.ABOUT_EXPIRE.getDbVal());

            return DashBoardStatisticDTO.builder()
                    .totalSigned(totalSigned)
                    .totalProcessing(totalProcessing)
                    .totalWaiting(totalWaiting)
                    .totalAboutExpire(totalAboutExpire)
                    .build();

        } catch (Exception e) {
            log.error("Error in getMyProcessDashboard: ", e);
            return null;
        }
    }

    public DashBoardStatisticDTO getMyContractDashboard(Authentication authentication, String fromDate, String toDate) {
        try {

            String email = authentication.getName();

            Customer customer = customerService.getCustomerByEmail(email);

            int totalSigned = contractRepository.countMyContractByStatus(customer.getId(), fromDate, toDate, ContractStatus.SIGNED.getDbVal());
            int totalProcessing = contractRepository.countMyContractByStatus(customer.getId(), fromDate, toDate, ContractStatus.PROCESSING.getDbVal());
            int totalRejected = contractRepository.countMyContractByStatus(customer.getId(), fromDate, toDate, ContractStatus.REJECTED.getDbVal());
            int totalCancelled = contractRepository.countMyContractByStatus(customer.getId(), fromDate, toDate, ContractStatus.CANCEL.getDbVal());
            int totalExpired = contractRepository.countMyContractByStatus(customer.getId(), fromDate, toDate, ContractStatus.EXPIRE.getDbVal());

            return DashBoardStatisticDTO.builder()
                    .totalSigned(totalSigned)
                    .totalProcessing(totalProcessing)
                    .totalReject(totalRejected)
                    .totalCancel(totalCancelled)
                    .totalExpires(totalExpired)
                    .build();

        } catch (Exception e) {
            log.error("Error in getMyContractDashboard: ", e);
            return null;
        }
    }

    public DashBoardStatisticDTO countContractByOrganization(String fromDate, String toDate, Integer organizationId) {
        try {

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

        } catch (Exception e) {
            log.error("Error in countContractByOrganization: ", e);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<ContractCustomerDTO> statisticsCustomerUseMaxContracts(){
        try{
            List<ContractCustomerDTO> response = new ArrayList<>();

            List<StaticCustomerUseContract> result = contractRepository.statisticsCustomerUseMaxContracts();

            if(result == null || result.isEmpty()){
                return null;
            }

            result.forEach(r -> {

                Customer customer = customerService.getCustomerById(r.getCustomerId());

                ContractCustomerDTO contractCustomerDTO = ContractCustomerDTO.builder()
                        .customerId(customer.getId())
                        .customerName(customer.getName())
                        .totalContracts(r.getTotal())
                        .build();

                response.add(contractCustomerDTO);
            });

            return response;

        } catch (Exception e){
            log.error("Error in countCompletedContracts: ", e);
            return null;
        }
    }
}
