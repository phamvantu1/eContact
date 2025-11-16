package com.ec.contract.service;

import com.ec.contract.constant.ContractStatus;
import com.ec.contract.mapper.TemplateContractMapper;
import com.ec.contract.model.dto.ContractChangeStatusRequest;
import com.ec.contract.model.dto.request.ContractRequestDTO;
import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.contract.model.entity.Customer;
import com.ec.contract.model.entity.TemplateContract;
import com.ec.contract.repository.TemplateContractRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateContractService {

    private final CustomerService customerService;
    private final TemplateContractRepository templateContractRepository;
    private final TemplateContractMapper templateContractMapper;

    @Transactional
    public ContractResponseDTO createTemplateContract(ContractRequestDTO requestDTO,
                                              Authentication authentication) {
        try {

            String email = authentication.getName();

            log.info("Creating contract for user: {}", email);

            Customer customer = customerService.getCustomerByEmail(email);

            TemplateContract contract = TemplateContract.builder()
                    .name(requestDTO.getName())
                    .contractNo(requestDTO.getContractNo())
                    .signTime(requestDTO.getSignTime())
                    .note(requestDTO.getNote())
                    .typeId(requestDTO.getTypeId())
                    .isTemplate(requestDTO.getIsTemplate())
                    .templateContractId(requestDTO.getTemplateContractId())
                    .contractExpireTime(requestDTO.getContractExpireTime())
                    .customerId(customer.getId())
                    .organizationId(customer.getOrganizationId())
                    .status(ContractStatus.DRAFT.getDbVal()) // tao mac dinh la draft
                    .build();

            var result = templateContractRepository.save(contract);

            return templateContractMapper.toDto(result);
        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create contract", e);
        }
    }

    public ContractResponseDTO changeContractStatus(Integer contractId, Integer status, Optional<ContractChangeStatusRequest> request) {
        try {

            log.info("contractId chuyển trạng thái {} , trạng thái muốn chuyển {} ", contractId, status);

            log.info("request {}", request.orElse(null));

            TemplateContract templateContract = templateContractRepository.findById(contractId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            templateContract.setStatus(status);

            var result = templateContractRepository.save(templateContract);

            return templateContractMapper.toDto(result);

        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to change template contract status", e);
        }
    }

}
