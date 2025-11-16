package com.ec.contract.service;

import com.ec.contract.constant.BaseStatus;
import com.ec.contract.mapper.TemplateDocumentMapper;
import com.ec.contract.model.dto.request.DocumentUploadDTO;
import com.ec.contract.model.dto.response.DocumentResponseDTO;
import com.ec.contract.model.entity.Contract;
import com.ec.contract.model.entity.Document;
import com.ec.contract.model.entity.TemplateContract;
import com.ec.contract.model.entity.TemplateDocument;
import com.ec.contract.repository.TemplateContractRepository;
import com.ec.contract.repository.TemplateDocumentRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateDocumentService {

    private final TemplateContractRepository templateContractRepository;
    private final TemplateDocumentMapper templateDocumentMapper;
    private final TemplateDocumentRepository templateDocumentRepository;
    private final MinioService minioService;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public DocumentResponseDTO createTemplateDocument(DocumentUploadDTO documentUploadDTO){
        try{
            TemplateContract contract = templateContractRepository.findById(documentUploadDTO.getContractId())
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            TemplateDocument document = TemplateDocument.builder()
                    .name(documentUploadDTO.getName())
                    .path(documentUploadDTO.getPath())        // chính là objectName trong MinIO
                    .fileName(documentUploadDTO.getFileName())
                    .bucketName(bucketName)
                    .contractId(documentUploadDTO.getContractId())
                    .type(documentUploadDTO.getType())
                    .status(documentUploadDTO.getStatus())
                    .build();

            TemplateDocument savedDocument = templateDocumentRepository.save(document);

            return templateDocumentMapper.toDto(savedDocument);
        } catch (CustomException e){
            throw e;
        } catch (Exception e){
            log.error("Error creating document record: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create document record", e);
        }
    }

    public Map<String, String> getPresignedUrl(Integer docId){
        try{
            TemplateDocument document = templateDocumentRepository.findById(docId)
                    .orElseThrow(() -> new CustomException(ResponseCode.DOCUMENT_NOT_FOUND));

            String url = minioService.getPresignedUrl(document.getBucketName(), document.getPath());

            return Map.of("message", url);
        } catch (CustomException e){
            throw e;
        } catch (Exception e){
            log.error("Error generating template presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    public List<DocumentResponseDTO> getTemplateDocumentByContract(Integer contractId){
        try{

            List<TemplateDocument> documents = templateDocumentRepository.findByContractIdAndStatus(contractId, BaseStatus.ACTIVE.ordinal());

            return templateDocumentMapper.toDtoList(documents);
        } catch (CustomException e){
            throw e;
        } catch (Exception e){
            log.error("Error fetching documents by contract ID {}: {}", contractId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch documents by contract ID", e);
        }
    }

}
