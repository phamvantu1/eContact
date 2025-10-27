package com.ec.contract.service;

import com.ec.contract.constant.ContractStatus;
import com.ec.contract.mapper.ContractMapper;
import com.ec.contract.model.Customer;
import com.ec.contract.model.dto.request.ContractRequestDTO;
import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.contract.model.entity.*;
import com.ec.contract.repository.ContractRefRepository;
import com.ec.contract.repository.ContractRepository;
import com.ec.contract.repository.FieldRepository;
import com.ec.contract.repository.ParticipantRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    @Autowired
    private ContractMapper contractMapper;

    private final ContractRepository contractRepository;
    private final ContractRefRepository contractRefRepository;
    private final CustomerService customerService;
    private final ParticipantRepository participantRepository;
    private final ParticipantService participantService;
    private final FieldRepository fieldRepository;

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
                    .status(ContractStatus.DRAFT.getDbVal())
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

    @Transactional(readOnly = true)
    public Contract getContractById(Integer contractId){
        try{

            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            log.info("Fetched contract: {}", contract);

            List<Participant> listParticipants =  participantRepository.findByContractIdOrderByOrderingAsc(contractId)
                    .stream().toList();

//            for(Participant participant: listParticipants) {
//                Set<Recipient> recipientSet = participant.getRecipients();
//
//                for(Recipient recipient : recipientSet) {
//                    Collection<Field> fieldCollection = fieldRepository.findAllByRecipientId(recipient.getId());
//                    recipient.setFields(Set.copyOf(fieldCollection));
//                }
//
//                participant.setRecipients(recipientSet);
//            }

//            contract.setParticipants(Set.copyOf(listParticipants));
//
//            List<ContractRef> contractRefList = contractRefRepository.findByContractId(contractId);
//
//            contract.setContractRefs(Set.copyOf(contractRefList));
//
//            var contractResponseDTO =  contractMapper.toDto(contract);
//
//            participantService.sortRecipient(contractResponseDTO.getParticipants());
//
//            return contractResponseDTO;

            return contract;

        }catch(CustomException ex){
            throw ex;
        }catch( Exception e){
            throw new RuntimeException("Failed to get contract by id {}", e);
        }
    }

    public ContractResponseDTO changeContractStatus(Integer contractId, Integer status){
        try{
            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            contract.setStatus(status);

            var result = contractRepository.save(contract);

            return contractMapper.toDto(result);
        }catch(CustomException ex){
            throw ex;
        }catch (Exception e){
            throw new RuntimeException("Failed to change contract status", e);
        }
    }


}
