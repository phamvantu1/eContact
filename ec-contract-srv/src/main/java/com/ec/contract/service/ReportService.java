package com.ec.contract.service;

import com.ec.contract.constant.BaseStatus;
import com.ec.contract.constant.ContractStatus;
import com.ec.contract.mapper.ContractMapper;
import com.ec.contract.mapper.ParticipantMapper;
import com.ec.contract.model.dto.OrganizationDTO;
import com.ec.contract.model.dto.response.*;
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

    @Transactional(readOnly = true)
    public Page<ContractResponseDTO> reportMyProcess(int organizationId, String fromDate, String toDate,
                                                    String completedFromDate, String completedToDate,
                                                    Integer status, String textSearch,
                                                    Integer page, Integer size) {
        try {
            List<Customer> customerList = customerService.getCustomerByOrganizationId(organizationId);

            List<String> emails = customerList.stream()
                    .map(Customer::getEmail)
                    .toList();

            Pageable pageable = PageRequest.of(page, size);

            Page<Contract> contractPage = contractRepository.reportMyProcess(emails, fromDate, toDate,
                    completedFromDate, completedToDate,
                    status, textSearch, pageable);

            List<ContractResponseDTO> result = contractMapper.toDtoList(contractPage.getContent());

            return new PageImpl<>(result, pageable, contractPage.getTotalElements());

        } catch (Exception e) {
            log.error("Error in reportByStatus: ", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public DashBoardStatisticDTO reportNumberByStatus(int organizationId, String fromDate, String toDate) {
        try {
            int cancelledCount = contractRepository.countContractByStatus(organizationId, fromDate, toDate, ContractStatus.CANCEL.getDbVal());
            int rejectedCount = contractRepository.countContractByStatus(organizationId, fromDate, toDate, ContractStatus.REJECTED.getDbVal());
            int expiredCount = contractRepository.countContractByStatus(organizationId, fromDate, toDate, ContractStatus.EXPIRE.getDbVal());
            int aboutExpireCount = contractRepository.countContractByStatus(organizationId, fromDate, toDate, ContractStatus.ABOUT_EXPIRE.getDbVal());
            int completedCount = contractRepository.countContractByStatus(organizationId, fromDate, toDate, ContractStatus.SIGNED.getDbVal());
            int processCount = contractRepository.countContractByStatus(organizationId, fromDate, toDate, ContractStatus.PROCESSING.getDbVal());
            return DashBoardStatisticDTO.builder()
                    .totalCancel(cancelledCount)
                    .totalReject(rejectedCount)
                    .totalExpires(expiredCount)
                    .totalAboutExpire(aboutExpireCount)
                    .totalSigned(completedCount)
                    .totalProcessing(processCount)
                    .build();
        } catch (Exception e) {
            log.error("Error in reportNumberByStatus: ", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<ReportByTypeDTO> reportNumberByType(int organizationId, String fromDate, String toDate) {
        try {

            List<Type> types = typeRepository.findByOrganizationIdAndStatus(organizationId, BaseStatus.ACTIVE.ordinal());

            List<ReportByTypeDTO> result = new ArrayList<>();
            for (Type type : types) {

                int countFinished = contractRepository.countContractByType(organizationId, fromDate, toDate, type.getId(), ContractStatus.SIGNED.getDbVal());
                int processingCount = contractRepository.countContractByType(organizationId, fromDate, toDate, type.getId(), ContractStatus.PROCESSING.getDbVal());
                int rejectedCount = contractRepository.countContractByType(organizationId, fromDate, toDate, type.getId(), ContractStatus.REJECTED.getDbVal());
                int cancelledCount = contractRepository.countContractByType(organizationId, fromDate, toDate, type.getId(), ContractStatus.CANCEL.getDbVal());
                int aboutExpireCount = contractRepository.countContractByType(organizationId, fromDate, toDate, type.getId(), ContractStatus.ABOUT_EXPIRE.getDbVal());
                int expiredCount = contractRepository.countContractByType(organizationId, fromDate, toDate, type.getId(), ContractStatus.EXPIRE.getDbVal());
                int totalCount = countFinished + processingCount + rejectedCount + cancelledCount + aboutExpireCount + expiredCount;

                DashBoardStatisticDTO statistic = DashBoardStatisticDTO.builder()
                        .totalSigned(countFinished)
                        .totalProcessing(processingCount)
                        .totalReject(rejectedCount)
                        .totalCancel(cancelledCount)
                        .totalAboutExpire(aboutExpireCount)
                        .totalExpires(expiredCount)
                        .total(totalCount)
                        .build();

                ReportByTypeDTO dto = ReportByTypeDTO.builder()
                        .typeName(type.getName())
                        .statistic(statistic)
                        .build();

                result.add(dto);
            }
            return result;
        } catch (Exception e) {
            log.error("Error in reportNumberByType: ", e);
            throw e;
        }
    }
}
