package com.ec.contract.service;

import com.ec.contract.constant.BaseStatus;
import com.ec.contract.constant.ContractStatus;
import com.ec.contract.mapper.TemplateContractMapper;
import com.ec.contract.mapper.TemplateParticipantMapper;
import com.ec.contract.model.dto.ContractChangeStatusRequest;
import com.ec.contract.model.dto.request.ContractRequestDTO;
import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.contract.model.entity.*;
import com.ec.contract.repository.TemplateContractRepository;
import com.ec.contract.repository.TemplateParticipantRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateContractService {

    private final CustomerService customerService;
    private final TemplateContractRepository templateContractRepository;
    private final TemplateContractMapper templateContractMapper;
    private final TemplateParticipantRepository templateParticipantRepository;
    private final TemplateParticipantMapper templateParticipantMapper;

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

            log.info("template contractId chuyển trạng thái {} , trạng thái muốn chuyển {} ", contractId, status);

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

    public Map<String, String> checkCodeUnique(String code) {
        try {
            Boolean isUnique = templateContractRepository.existsByContractNo(code);
            return Map.of("isExist", String.valueOf(isUnique));
        } catch (Exception e) {
            log.error("Error checking code uniqueness: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to check code uniqueness", e);
        }
    }

    public Map<String,String> deleteTemplateContract(Integer contractId) {
        try {
            log.info("Deleting template contract with ID: {}", contractId);

            TemplateContract templateContract = templateContractRepository.findById(contractId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            templateContract.setStatus(BaseStatus.IN_ACTIVE.ordinal());

            templateContractRepository.save(templateContract);

            return Map.of("message", "Template contract deleted successfully");
        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Error deleting template contract: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete template contract", e);
        }
    }

    @Transactional(readOnly = true)
    public ContractResponseDTO getTemplateContractById(Integer contractId) {
        try {
            log.info("Fetching template contract with ID: {}", contractId);

            TemplateContract templateContract = templateContractRepository.findById(contractId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            // Lấy danh sách participant theo hợp đồng
            List<TemplateParticipant> listParticipants = templateParticipantRepository.findByContractIdOrderByOrderingAsc(contractId).stream().toList();

            // Map entity sang DTO
            var contractResponseDTO = templateContractMapper.toDto(templateContract);

            contractResponseDTO.setParticipants(new HashSet<>(templateParticipantMapper.toDtoList(listParticipants)));

            return contractResponseDTO;
        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Error fetching template contract: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch template contract", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ContractResponseDTO> getMyTemplateContracts(Authentication authentication,
                                                            Integer type,
                                                            String name,
                                                            Integer size,
                                                            Integer page) {
        try {
            String email = authentication.getName();

            Pageable pageable = PageRequest.of(page, size);

            log.info("Fetching template contracts for user: {}", email);

            Customer customer = customerService.getCustomerByEmail(email);

            Page<TemplateContract> templateContracts = templateContractRepository.getMyTemplateContracts(customer.getId(), type, name,pageable );

            return templateContractMapper.toDtoList(templateContracts.getContent());
        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Error fetching my template contracts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch my template contracts", e);
        }
    }

    @Transactional
    public ContractResponseDTO updateTemplateContract(Integer contractId,
                                              ContractRequestDTO requestDTO,
                                              Authentication authentication) {
        try {

            String email = authentication.getName();

            log.info("Updating template contract with ID: {} for user: {}", contractId, email);

            TemplateContract templateContract = templateContractRepository.findById(contractId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            templateContract.setName(requestDTO.getName());
            templateContract.setContractNo(requestDTO.getContractNo());
            templateContract.setSignTime(requestDTO.getSignTime());
            templateContract.setNote(requestDTO.getNote());
            templateContract.setTypeId(requestDTO.getTypeId());
            templateContract.setIsTemplate(requestDTO.getIsTemplate());
            templateContract.setTemplateContractId(requestDTO.getTemplateContractId());
            templateContract.setContractExpireTime(requestDTO.getContractExpireTime());

            var result = templateContractRepository.save(templateContract);

            return templateContractMapper.toDto(result);
        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update template contract", e);
        }
    }

}
