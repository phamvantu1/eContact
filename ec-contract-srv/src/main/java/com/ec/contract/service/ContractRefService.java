package com.ec.contract.service;

import com.ec.contract.constant.ContractStatus;
import com.ec.contract.model.dto.response.RefDTO;
import com.ec.contract.repository.ContractRefRepository;
import com.ec.contract.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractRefService {

    private final ContractService contractService;
    private final ContractRefRepository contractRefRepository;
    private final ContractRepository contractRepository;


    public Page<RefDTO> getAllContractRefs(Integer page, Integer size){
        try{
            Pageable pageable = PageRequest.of(page, size);
            var contractPage = contractRepository.findByStatus(ContractStatus.SIGNED.getDbVal(), pageable);

            return  contractPage.map(contract -> {
                RefDTO refDTO = new RefDTO();
                refDTO.setId(contract.getId());
                refDTO.setName(contract.getName());
                return refDTO;
            });

        } catch (Exception e){
            log.error("Error in getAllContractRefs: ", e);
            throw e;
        }
    }
}
