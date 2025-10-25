package com.ec.contract.service;

import com.ec.contract.constant.ContractStatus;
import com.ec.contract.mapper.ContractMapper;
import com.ec.contract.model.Customer;
import com.ec.contract.model.dto.request.ContractRequestDTO;
import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.contract.model.entity.Contract;
import com.ec.contract.model.entity.ContractRef;
import com.ec.contract.repository.ContractRefRepository;
import com.ec.contract.repository.ContractRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    @Autowired
    private ContractMapper contractMapper;

    private final ContractRepository contractRepository;
    private final DocumentService documentService;
    private final ContractRefRepository contractRefRepository;
    private final CustomerService customerService;

    public Map<String, String> checkCodeUnique(String code){
        try{
            Boolean isUnique = contractRepository.existsByContractNo(code);
            return Map.of("isUnique", String.valueOf(isUnique));
        } catch (Exception e){
            log.error("Error checking code uniqueness: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to check code uniqueness", e);
        }
    }

    @Transactional
    public ContractResponseDTO createContract(ContractRequestDTO requestDTO,
                                              Authentication authentication){
        try{

            String email = authentication.getName();

            log.info("Creating contract for user: {}", email);

            Customer customer = customerService.getCustomerByEmail(email);

            Contract contract = Contract.builder()
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
                    .status(ContractStatus.DRAFF.getDbVal())
                    .build();

            if (!requestDTO.getContractRefs().isEmpty()){
                Set<ContractRef> contractRefs = new HashSet<>();
                for (var ref : requestDTO.getContractRefs()){
                    if (ref.getRefId() == null){
                        throw new CustomException(ResponseCode.CONTRACT_REF_NOT_FOUND);
                    }
                    ContractRef contractRef = contractRefRepository.findById(ref.getRefId())
                            .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_REF_NOT_FOUND));
                    contractRefs.add(contractRef);
                }
                contract.setContractRefs(contractRefs);
            }

            var result = contractRepository.save(contract);

            return contractMapper.toDto(result);
        }catch(CustomException ex){
            throw ex;
        }catch (Exception e){
            throw new RuntimeException("Failed to create contract", e);
        }
    }


}
