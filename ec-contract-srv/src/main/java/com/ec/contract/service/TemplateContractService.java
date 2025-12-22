package com.ec.contract.service;

import com.ec.contract.constant.BaseStatus;
import com.ec.contract.constant.ContractStatus;
import com.ec.contract.constant.DocumentType;
import com.ec.contract.mapper.TemplateContractMapper;
import com.ec.contract.mapper.TemplateParticipantMapper;
import com.ec.contract.model.dto.ContractChangeStatusRequest;
import com.ec.contract.model.dto.request.ContractRequestDTO;
import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.contract.model.entity.*;
import com.ec.contract.repository.TemplateContractRepository;
import com.ec.contract.repository.TemplateDocumentRepository;
import com.ec.contract.repository.TemplateParticipantRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateContractService {

    private final CustomerService customerService;
    private final TemplateContractRepository templateContractRepository;
    private final TemplateContractMapper templateContractMapper;
    private final TemplateParticipantRepository templateParticipantRepository;
    private final TemplateParticipantMapper templateParticipantMapper;
    private final TemplateDocumentService templateDocumentService;
    private final TemplateDocumentRepository templateDocumentRepository;
    private final ChangeFileService changeFileService;
    private final FileService fileService;

    @Value("${application.temporary.directory}")
    private String tempFolder;

    @Value("${minio.bucket-name}")
    private String bucketName;

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

            log.info("template request {}", request.orElse(null));

            TemplateContract templateContract = templateContractRepository.findById(contractId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            templateContract.setStatus(status);

            var result = templateContractRepository.save(templateContract);

            final var contractStatusOptional = Arrays.stream(ContractStatus.values())
                    .filter(contractStatus -> contractStatus.getDbVal().equals(status))
                    .findFirst();

            var response = templateContractMapper.toDto(result);

            if (contractStatusOptional.isPresent()) {
                switch (contractStatusOptional.get()) {
                    case CREATED: // trường hợp chuyển từ trạng thái hợp đồng nháp sang tạo hợp đồng
                        log.info("bat dau chuyen trang thai tao hop dong: {}", contractStatusOptional.get());
                        try {
                            issue(response);
                        } catch (Exception e) {
                            throw new CustomException(ResponseCode.FAIL_CHANGE_CONTRACT_STATUS);
                        }
                        break;
                    case CANCEL:
                        log.info("bat dau chuyen trang thai huy hop dong: {}", contractStatusOptional.get());
                        break;
                }
            }

            return response;

        } catch (CustomException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to change template contract status", e);
        }
    }

    private void issue(ContractResponseDTO contractDto) {

        try {

            byPassContractUid(contractDto.getId());

        } catch (Exception e) {
            log.error("Failed to issue contract: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to issue contract", e);
        }
    }

    @Transactional
    public void byPassContractUid(int contractId) {
        log.info("Bắt đầu thêm contractUid vào pdf hợp đồng");
        String tempFilePath = null;
        String pathFileBackUp = null;
        String pathMinio = null;
        String bucketMinio = null;

        try {
            // lấy thông tin hợp đồng
            final var contractOptional = templateContractRepository.findById(contractId);

            if(contractOptional.isPresent()){
                final var contract = contractOptional.get();

                final var documentCollection = templateDocumentRepository.findAllByContractIdAndStatusOrderByIdDesc(
                        contractId, BaseStatus.ACTIVE.ordinal()
                );

                final var docOptional = documentCollection.stream()
                        .filter(document -> Objects.equals(document.getType(), DocumentType.PRIMARY.getDbVal()))
                        .findFirst();

                if (docOptional.isPresent()) {
                    final var doc = docOptional.get();

                    Map<String,String> mapPresignedUrl = templateDocumentService.getPresignedUrl(doc.getId());

                    final var presignedUrl = mapPresignedUrl.get("message");

                    try {
                        log.info("Bắt đầu tạo ra file backup trước khi add ô text");
                        final var urlFileBefore = presignedUrl;
                        byte[] fileBeforeAction = new URL(urlFileBefore).openStream().readAllBytes();
                        String fileUrl = String.format("%s/%s_bak.pdf", tempFolder, UUID.randomUUID());
                        try (FileOutputStream fileOutputStream = new FileOutputStream(fileUrl)) {
                            fileOutputStream.write(fileBeforeAction);
                        }

                        // đảm bảo phải ghi file thành công mới đến bước set đường dẫn
                        pathFileBackUp = fileUrl;
                        bucketMinio = bucketName;
                        pathMinio = doc.getPath();
                        log.info("Tạo file backup thành công");
                    }catch (Exception ignored){}

                    tempFilePath = changeFileService.addText(
                            presignedUrl,
                            1,
                            15,
                            0,
                            25,
                            150,
                            "Times New Roman",
                            11,
                            "Mã HD: " + contract.getContractNo(),
                            true
                    );

                    log.info("tmp file: " + tempFilePath);

                    if (!StringUtils.hasText(tempFilePath)) {
                        throw new RuntimeException("Add contractUid thất bại");
                    }

                    // thay thế nội dung tệp tin trên hệ thống MinIO
                    var res = fileService.replaceTemplate(tempFilePath, doc.getId());

                    log.info("replace file: " + !res.isEmpty());
                } else {
                    log.error("by pass add text error: can't find document where contract_id = {}", contractId);
                }
            } else {
                log.error("Can't find contract_id = {}", contractId);
            }
        } catch (Exception e) {
            log.error("Lỗi tại hàm byPassContractUid bắt đầu backup File ban đầu");
            fileService.backUpFileMinioIfErrorAction(pathFileBackUp, contractId);
            log.error("can't by pass add text thth: ", e);
            throw new RuntimeException(e.getMessage());
        } finally {
            if (StringUtils.hasText(tempFilePath)) {
                try {
                    FileUtils.deleteDirectory(new File(tempFilePath));
                } catch (Exception e) {
                    log.error("can't delete directory {}", tempFilePath);
                }
            }
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
    public Page<ContractResponseDTO> getMyTemplateContracts(Authentication authentication,
                                                            Integer type,
                                                            String name,
                                                            Integer size,
                                                            Integer page) {
        try {
            String email = authentication.getName();

            Pageable pageable = PageRequest.of(page, size);

            log.info("Fetching template contracts for user: {}", email);

            Customer customer = customerService.getCustomerByEmail(email);

            Page<TemplateContract> templateContracts = templateContractRepository.getMyTemplateContracts(customer.getId(), type, name,pageable);

            return templateContracts.map(templateContractMapper::toDto);
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
