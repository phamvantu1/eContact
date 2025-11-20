package com.ec.contract.service;

import com.ec.contract.mapper.ContractMapper;
import com.ec.contract.mapper.ParticipantMapper;
import com.ec.contract.model.dto.OrganizationDTO;
import com.ec.contract.model.dto.response.ContractRefResponseDTO;
import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.contract.model.dto.response.ReportDetailDTO;
import com.ec.contract.model.entity.*;
import com.ec.contract.repository.ContractRefRepository;
import com.ec.contract.repository.ContractRepository;
import com.ec.contract.repository.TypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {

    private final ContractRepository contractRepository;
    private final TypeRepository typeRepository;
    private final ContractRefRepository contractRefRepository;
    private final CustomerService customerService;
    private final ParticipantMapper participantMapper;
    private final ContractMapper contractMapper;

    @Transactional(readOnly = true)
    public Page<ReportDetailDTO> reportDetail(int organizationId, String fromDate, String toDate,
                                              String completedFromDate, String completedToDate,
                                              Integer status, String textSearch,
                                              Integer page, Integer size) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            Page<Contract> contractPage = contractRepository.reportDetail(organizationId, fromDate, toDate,
                    completedFromDate, completedToDate, status, textSearch, pageable);

            List<ReportDetailDTO> result = new ArrayList<>();

            for (Contract contract : contractPage.getContent()) {

                Type type = typeRepository.findById(contract.getId() == null ? 0 : contract.getId()).orElse(null);

                List<ContractRef> contractRefs = contractRefRepository.findByContractId(contract.getId());

                List<ContractRefResponseDTO> refs = new ArrayList<>();

                for (ContractRef ref : contractRefs) {
                    Contract contractRef = contractRepository.findById(ref.getRefId()).orElse(null);
                    ContractRefResponseDTO refDTO = ContractRefResponseDTO.builder()
                            .id(ref.getId())
                            .refId(ref.getRefId())
                            .refName(contractRef != null ? contractRef.getName() : null)
                            .build();
                    refs.add(refDTO);
                }

                Customer customer = customerService.getCustomerById(contract.getCustomerId());

                OrganizationDTO organizationDTO = customerService.getOrganizationById(contract.getOrganizationId());

                ReportDetailDTO contractDetail = ReportDetailDTO.builder()
                        .id(contract.getId())
                        .name(contract.getName())
                        .typeName(type != null ? type.getName() : null)
                        .contractNo(contract.getContractNo())
                        .refs(refs)
                        .createdAt(contract.getCreatedAt())
                        .contractExpireTime(contract.getContractExpireTime())
                        .status(contract.getStatus())
                        .completeDate(contract.getStatus() == 30 ? contract.getUpdatedAt() : null)
                        .cancelDate(contract.getStatus() == 32 ? contract.getCancelDate() : null)
                        .updatedAt(contract.getUpdatedAt())
                        .customer(customer != null ? customer.getName() : null)
                        .participants(participantMapper.toDtoList((List<Participant>) contract.getParticipants()))
                        .organizationCreatedName(organizationDTO != null ? organizationDTO.getName() : null)
                        .build();

                result.add(contractDetail);
            }

            return new PageImpl<>(result, pageable, contractPage.getTotalElements());

        } catch (Exception e) {
            log.error("Error in reportDetail: ", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<ContractResponseDTO> reportByStatus(int organizationId, String fromDate, String toDate,
                                                    String completedFromDate, String completedToDate,
                                                    Integer status, String textSearch,
                                                    Integer page, Integer size) {
        try {

            Pageable pageable = PageRequest.of(page, size);

            Page<Contract> contractPage = contractRepository.reportByStatus(organizationId, fromDate, toDate,
                                                                            completedFromDate, completedToDate,
                                                                            status, textSearch, pageable);

            List<ContractResponseDTO> result = contractMapper.toDtoList(contractPage.getContent());

            return new PageImpl<>(result, pageable, contractPage.getTotalElements());

        } catch (Exception e) {
            log.error("Error in reportByStatus: ", e);
            throw e;
        }
    }
}
