package com.ec.contract.service;

import com.ec.contract.constant.ContractStatus;
import com.ec.contract.constant.RecipientRole;
import com.ec.contract.mapper.ContractMapper;
import com.ec.contract.model.dto.BpmnFlowRes;
import com.ec.contract.model.dto.BpmnRecipientDto;
import com.ec.contract.model.dto.ParticipantDTO;
import com.ec.contract.model.dto.request.ContractRequestDTO;
import com.ec.contract.model.dto.request.FilterContractDTO;
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
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private ModelMapper modelMapper;

    private final ContractRepository contractRepository;
    private final ContractRefRepository contractRefRepository;
    private final CustomerService customerService;
    private final ParticipantRepository participantRepository;
    private final ParticipantService participantService;
    private final FieldRepository fieldRepository;

    public Map<String, String> checkCodeUnique(String code) {
        try {
            Boolean isUnique = contractRepository.existsByContractNo(code);
            return Map.of("isUnique", String.valueOf(isUnique));
        } catch (Exception e) {
            log.error("Error checking code uniqueness: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to check code uniqueness", e);
        }
    }

    @Transactional
    public ContractResponseDTO createContract(ContractRequestDTO requestDTO,
                                              Authentication authentication) {
        try {

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
                    .status(ContractStatus.DRAFT.getDbVal()) // tao mac dinh la draft
                    .build();

            if (!requestDTO.getContractRefs().isEmpty()) {
                Set<ContractRef> contractRefs = new HashSet<>();
                for (var ref : requestDTO.getContractRefs()) {
                    if (ref.getRefId() == null) {
                        throw new CustomException(ResponseCode.CONTRACT_REF_NOT_FOUND);
                    }

                    Contract checkContractRef = contractRepository.findById(ref.getRefId())
                            .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_REF_NOT_FOUND));

                    ContractRef contractRef = ContractRef.builder()
                            .contract(contract)
                            .refId(ref.getRefId())
                            .build();

                    contractRefs.add(contractRef);
                }
                contract.setContractRefs(contractRefs);
            }

            var result = contractRepository.save(contract);

            return contractMapper.toDto(result);
        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create contract", e);
        }
    }

    @Transactional(readOnly = true)
    public ContractResponseDTO getContractById(Integer contractId) {
        try {

            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            log.info("Fetched contract: {}", contract);

            List<Participant> listParticipants = participantRepository.findByContractIdOrderByOrderingAsc(contractId)
                    .stream().toList();

            for (Participant participant : listParticipants) {
                Set<Recipient> recipientSet = participant.getRecipients();

                for (Recipient recipient : recipientSet) {
                    Collection<Field> fieldCollection = fieldRepository.findAllByRecipientId(recipient.getId());
                    recipient.setFields(Set.copyOf(fieldCollection));
                }

                participant.setRecipients(recipientSet);
            }

            contract.setParticipants(Set.copyOf(listParticipants));

            List<ContractRef> contractRefList = contractRefRepository.findByContractId(contractId);

            contract.setContractRefs(Set.copyOf(contractRefList));

            var contractResponseDTO = contractMapper.toDto(contract);

            participantService.sortRecipient(contractResponseDTO.getParticipants());

            return contractResponseDTO;

        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get contract by id {}", e);
        }
    }

    public ContractResponseDTO changeContractStatus(Integer contractId, Integer status) {
        try {
            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            contract.setStatus(status);

            var result = contractRepository.save(contract);

            List<Participant> listParticipants = participantRepository.findByContractIdOrderByOrderingAsc(contractId)
                    .stream().toList();

            for (Participant participant : listParticipants) {
                Set<Recipient> recipientSet = participant.getRecipients();

                for (Recipient recipient : recipientSet) {
                    Collection<Field> fieldCollection = fieldRepository.findAllByRecipientId(recipient.getId());
                    recipient.setFields(Set.copyOf(fieldCollection));
                }

                participant.setRecipients(recipientSet);
            }

            contract.setParticipants(Set.copyOf(listParticipants));

            List<ContractRef> contractRefList = contractRefRepository.findByContractId(contractId);

            contract.setContractRefs(Set.copyOf(contractRefList));

            var contractResponseDTO = contractMapper.toDto(contract);

            participantService.sortRecipient(contractResponseDTO.getParticipants());

            return contractResponseDTO;

        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to change contract status", e);
        }
    }

    public Page<ContractResponseDTO> getMyContracts(Authentication authentication,
                                                    FilterContractDTO filterContractDTO) {
        try {
            String email = authentication.getName();

            Customer customer = customerService.getCustomerByEmail(email);

            Pageable pageable = PageRequest.of(filterContractDTO.getPage(), filterContractDTO.getSize());

            Page<Contract> contractPage = contractRepository.findMyContracts(
                    customer.getId(),
                    filterContractDTO.getStatus(),
                    filterContractDTO.getTextSearch(),
                    filterContractDTO.getFromDate(),
                    filterContractDTO.getToDate(),
                    pageable);

            List<ContractResponseDTO> contractResponseDTOList = contractPage.getContent()
                    .stream()
                    .map(contractMapper::toDto)
                    .toList();

            return new PageImpl<>(contractResponseDTOList, contractPage.getPageable(), contractPage.getTotalElements());
        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get my contracts", e);
        }
    }

    public Page<ContractResponseDTO> getMyProcessContracts(Authentication authentication,
                                                           FilterContractDTO filterContractDTO) {
        try {
            String email = authentication.getName();

            List<Integer> newListStatus = new ArrayList<>();

            switch (filterContractDTO.getStatus()) {
                case 1 -> newListStatus = Arrays.asList(10, 20);
                case 2 -> newListStatus = Arrays.asList(30, 40, 31, 32);
                // Có thể thêm nhiều case khác nếu cần
                default -> newListStatus = Collections.emptyList(); // hoặc giữ nguyên list trống
            }

            Pageable pageable = PageRequest.of(filterContractDTO.getPage(), filterContractDTO.getSize());

            Page<Contract> contractPage = contractRepository.findMyProcessContracts(
                    email,
                    newListStatus,
                    filterContractDTO.getTextSearch(),
                    filterContractDTO.getFromDate(),
                    filterContractDTO.getToDate(),
                    pageable);

            List<ContractResponseDTO> contractResponseDTOList = contractPage.getContent()
                    .stream()
                    .map(contractMapper::toDto)
                    .toList();

            return new PageImpl<>(contractResponseDTOList, contractPage.getPageable(), contractPage.getTotalElements());
        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get my process contracts", e);
        }
    }

    public Page<ContractResponseDTO> getContractsByOrganization(
            FilterContractDTO filterContractDTO) {
        try {
            Pageable pageable = PageRequest.of(filterContractDTO.getPage(), filterContractDTO.getSize());

            List<Customer> listCustomers = customerService.getCustomerByOrganizationId(filterContractDTO.getOrganizationId());
            List<Integer> listCustomerIds = new ArrayList<>();
            for (Customer customer : listCustomers) {
                listCustomerIds.add(customer.getId());
            }

            if (listCustomerIds.isEmpty()) {
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }

            Page<Contract> contractPage = contractRepository.findContractsByOrganization(
                    listCustomerIds,
                    filterContractDTO.getStatus(),
                    filterContractDTO.getTextSearch(),
                    filterContractDTO.getFromDate(),
                    filterContractDTO.getToDate(),
                    pageable);

            List<ContractResponseDTO> contractResponseDTOList = contractPage.getContent()
                    .stream()
                    .map(contractMapper::toDto)
                    .toList();

            return new PageImpl<>(contractResponseDTOList, contractPage.getPageable(), contractPage.getTotalElements());
        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get contracts by organization", e);
        }
    }

    public ContractResponseDTO updateContract(Integer contractId,
                                              ContractRequestDTO requestDTO) {
        try {
            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            contract.setName(requestDTO.getName());
            contract.setNote(requestDTO.getNote());
            contract.setContractNo(requestDTO.getContractNo());
            contract.setSignTime(requestDTO.getSignTime());
            contract.setContractRefs(contract.getContractRefs());
            contract.setTypeId(requestDTO.getTypeId());
            contract.setIsTemplate(requestDTO.getIsTemplate());
            contract.setTemplateContractId(requestDTO.getTemplateContractId());
            contract.setContractExpireTime(requestDTO.getContractExpireTime());

            var result = contractRepository.save(contract);

            return contractMapper.toDto(result);
        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update contract", e);
        }
    }

    public BpmnFlowRes getBpmnFlowByContractId(Integer contractId) {

        try {
            var res = new BpmnFlowRes();

            Contract contract = contractRepository.findById(contractId).orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            // set participant, recipient, field vao trong contract
            List<Participant> listParticipants = participantRepository.findByContractIdOrderByOrderingAsc(contractId)
                    .stream().toList();

            for (Participant participant : listParticipants) {
                Set<Recipient> recipientSet = participant.getRecipients();

                for (Recipient recipient : recipientSet) {
                    Collection<Field> fieldCollection = fieldRepository.findAllByRecipientId(recipient.getId());
                    recipient.setFields(Set.copyOf(fieldCollection));
                }

                participant.setRecipients(recipientSet);
            }

            contract.setParticipants(Set.copyOf(listParticipants));

            ContractResponseDTO contractDto = contractMapper.toDto(contract);

            List<ParticipantDTO> participantResult = new ArrayList<>();
            List<BpmnRecipientDto> bpmnRecipientList = new ArrayList<>();
            List<BpmnRecipientDto> normalRecipientList = new ArrayList<>();
            List<BpmnRecipientDto> cancelRecipientList = new ArrayList<>();
            res.setRecipients(bpmnRecipientList);

            var customer = customerService.getCustomerById(contractDto.getCreatedBy());

            BpmnRecipientDto createdBy = new BpmnRecipientDto();
            createdBy.setId(customer.getId());
            createdBy.setName(customer.getName());
            createdBy.setEmail(customer.getEmail());

            res.setCreatedBy(createdBy);
            res.setReasonCancel(contractDto.getReasonReject());
            res.setCreatedAt(contractDto.getCreatedAt());
            res.setContractStatus(contractDto.getStatus());

            for (var participant : contractDto.getParticipants()) {
                participantResult.add(participant);

                log.info("Processing participant: {}", participant.getName());

                for (var recipient : participant.getRecipients()) {
                    BpmnRecipientDto bpmnFlowRes;

                    bpmnFlowRes = modelMapper.map(recipient, BpmnRecipientDto.class);
                    bpmnFlowRes.setParticipant(participant);
                    bpmnFlowRes.setParticipantName(participant.getName());
                    bpmnFlowRes.setParticipantOrder(participant.getOrdering());
                    bpmnFlowRes.setParticipantType(participant.getType());
                    var organization = customerService.getOrganizationByCustomerEmail(recipient.getEmail());
                    var userInOrganization = participant.getName();
                    if (organization != null) {
                        userInOrganization = organization.getName();
                    }
                    bpmnFlowRes.setUserInOrganization(userInOrganization);

                    bpmnFlowRes.setRecipientHistory(false);

                    if (contractDto.getStatus() == 32 || contractDto.getStatus() == 31) {
                        if (recipient.getProcessAt() != null) {
                            normalRecipientList.add(bpmnFlowRes);
                        }
                    } else {
                        normalRecipientList.add(bpmnFlowRes);

                    }
                }
            }

            // nếu là hợp đồng hủy bỏ thì đưa vào luồng xử lý hiển thị người hủy bỏ, thời gian hủy bỏ và lý do hủy bỏ
            if (contractDto.getStatus() == 32) {
                BpmnRecipientDto bpmnFlowResCancel;

                bpmnFlowResCancel = modelMapper.map(customer, BpmnRecipientDto.class);
                bpmnFlowResCancel.setStatus(3);
                bpmnFlowResCancel.setRole(RecipientRole.CANCELLER.getDbVal());
                bpmnFlowResCancel.setOrdering(0);
                bpmnFlowResCancel.setProcessAt(contractDto.getUpdatedAt());
                bpmnFlowResCancel.setReasonReject(contractDto.getReasonReject());
                bpmnFlowResCancel.setRecipientHistory(true);
                cancelRecipientList.add(bpmnFlowResCancel);
            }


            // Sort phần tử không phải history
            Collections.sort(normalRecipientList);

            // Ghép lại danh sách: history lên đầu, rồi đến danh sách bình thường đã sort
//        bpmnRecipientList.addAll(historyRecipientList);
            bpmnRecipientList.addAll(normalRecipientList);
            bpmnRecipientList.addAll(cancelRecipientList);

            return res;
        } catch (Exception e) {
            log.error("Failed to get BPMN flow by contract id: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get BPMN flow by contract id", e);
        }

    }


}
