package com.ec.contract.service;

import com.ec.contract.constant.ContractStatus;
import com.ec.contract.constant.RecipientRole;
import com.ec.contract.mapper.ContractMapper;
import com.ec.contract.mapper.ParticipantMapper;
import com.ec.contract.model.dto.*;
import com.ec.contract.model.dto.request.ContractRequestDTO;
import com.ec.contract.model.dto.request.FilterContractDTO;
import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.contract.model.entity.*;
import com.ec.contract.repository.*;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
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
    private final CustomerService customerService;
    private final ParticipantRepository participantRepository;
    private final ParticipantService participantService;
    private final FieldRepository fieldRepository;
    private final DocumentRepository documentRepository;
    private final ChangeFileService changeFileService;
    private final ParticipantMapper participantMapper;

    private BpmnService bpmnService; // không final

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${application.temporary.directory}")
    private String tempFolder;

    @Autowired
    public void setBpmnService(@Lazy BpmnService bpmnService) {
        this.bpmnService = bpmnService;
    }

    public Map<String, String> checkCodeUnique(String code) {
        try {
            Boolean isUnique = contractRepository.existsByContractNo(code);
            return Map.of("isExist", String.valueOf(isUnique));
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

            Contract contract = new Contract(); // tạo instance mới
            contract.setName(requestDTO.getName());
            contract.setContractNo(requestDTO.getContractNo());
            contract.setSignTime(requestDTO.getSignTime());
            contract.setNote(requestDTO.getNote());
            contract.setTypeId(requestDTO.getTypeId());
            contract.setIsTemplate(requestDTO.getIsTemplate());
            contract.setTemplateContractId(requestDTO.getTemplateContractId());
            contract.setContractExpireTime(requestDTO.getContractExpireTime());
            contract.setCustomerId(customer.getId());
            contract.setOrganizationId(customer.getOrganizationId());
            contract.setStatus(ContractStatus.DRAFT.getDbVal()); // mặc định draft

            contract.setParticipants(new HashSet<>());

            if (requestDTO.getContractRefs() != null && !requestDTO.getContractRefs().isEmpty()) {
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

            log.info("=====start get contract by id :{}", contractId);

            // Lấy hợp đồng
            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            // Lấy danh sách participant theo hợp đồng
            List<Participant> listParticipants = participantRepository.findByContractIdOrderByOrderingAsc(contractId).stream().toList();

            // Map entity sang DTO
            var contractResponseDTO = contractMapper.toDto(contract);

            contractResponseDTO.setParticipants(new HashSet<>(participantMapper.toDtoList(listParticipants)));

            // Sắp xếp recipient trong DTO
            participantService.sortRecipient(contractResponseDTO.getParticipants());

            // Lấy thông tin refName và path cho contractRefs
            contractResponseDTO.getContractRefs().forEach(ref -> {
                Contract refContract = contractRepository.findById(ref.getRefId()).orElse(null);
                if (refContract != null) {
                    ref.setRefName(refContract.getName());
                    Document document = documentRepository.findByContractIdAndType(refContract.getId(), 1);
                    ref.setPath(document != null ? document.getPath() : null);
                }
            });

            return contractResponseDTO;

        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get contract by id : ", e);
        }
    }

    public ContractResponseDTO changeContractStatus(Integer contractId, Integer status, Optional<ContractChangeStatusRequest> request) {
        try {

            log.info("contractId chuyển trạng thái {} , trạng thái muốn chuyển {} ", contractId, status);

            log.info("request {}", request.orElse(null));

            // Thay đổi trạng thái hợp đồng
            final var contract = changeStatus(
                    contractId, status, request.orElse(null)
            );

            if (contract.isPresent()) {
                log.info("start ---change status -----");
                final var contractStatusOptional = Arrays.stream(ContractStatus.values())
                        .filter(contractStatus -> contractStatus.getDbVal().equals(status))
                        .findFirst();

                if (contractStatusOptional.isPresent()) {
                    switch (contractStatusOptional.get()) {
                        case CREATED: // trường hợp chuyển từ trạng thái hợp đồng nháp sang tạo hợp đồng
                            log.info("bat dau chuyen trang thai tao hop dong: {}", contractStatusOptional.get());
                            try {
                                issue(contract.get());
                            } catch (Exception e) {
                                throw new CustomException(ResponseCode.FAIL_CHANGE_CONTRACT_STATUS);
                            }
                            break;
                        case CANCEL:
                            log.info("bat dau chuyen trang thai huy hop dong: {}", contractStatusOptional.get());
                            // Khởi tạo luồng huỷ HĐ
                            bpmnService.cancelContract(contract.get());
                            break;
                    }
                }
            }

            return contract.get();

        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to change contract status hhhhhausghf", e);
        }
    }

    private void issue(ContractResponseDTO contractDto) {

        try {
            int contractId = contractDto.getId();

            // Khởi tạo luồng xử lý HĐ
            bpmnService.startContract(contractDto);

            // Cập nhật trạng thái HĐ thành PROCESSING
            changeStatus(contractId, ContractStatus.PROCESSING.getDbVal(), null);

            changeFileService.byPassContractUid(contractDto.getId());

//            changeFileService.byPassContractNo(contractDto);

        } catch (Exception e) {
            log.error("Failed to issue contract: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to issue contract", e);
        }
    }

    @Transactional
    public Optional<ContractResponseDTO> changeStatus(Integer contractId, Integer status, ContractChangeStatusRequest request) {

        try {
            log.info("[changeStatus] contractId: {}, status: {}", contractId, status);

            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            contract.setStatus(status);

            if (request != null && StringUtils.hasText(request.getReason())) {
                contract.setReasonReject(request.getReason());
                contract.setCancelDate(LocalDateTime.now());
            }

            contract = contractRepository.save(contract);

            var result = contractMapper.toDto(contract);

            return Optional.of(result);

        } catch (Exception e) {
            log.error("Failed to change contract status saSSSSSS: {}",  e.getMessage(), e);
            throw new RuntimeException("Failed to change contract status agaaaaaa", e);
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
                    filterContractDTO.getStatus(),
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

    public void sortParallel(ContractResponseDTO contractDto, List<RecipientDTO> recipients) {
        Collections.sort(recipients, Comparator
                .<RecipientDTO>comparingInt(recipient -> recipient.getParticipant().getOrdering())
                .thenComparingInt(RecipientDTO::getRole)
                .thenComparingInt(RecipientDTO::getOrdering)
        );
    }


}
