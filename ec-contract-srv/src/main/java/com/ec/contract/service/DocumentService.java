package com.ec.contract.service;

import com.ec.contract.constant.BaseStatus;
import com.ec.contract.constant.DocumentType;
import com.ec.contract.mapper.DocumentMapper;
import com.ec.contract.model.dto.request.DocumentUploadDTO;
import com.ec.contract.model.dto.response.DocumentResponseDTO;
import com.ec.contract.model.entity.Contract;
import com.ec.contract.model.entity.Document;
import com.ec.contract.repository.ContractRepository;
import com.ec.contract.repository.DocumentRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final MinioService minioService;
    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final ContractRepository contractRepository;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public DocumentResponseDTO uploadDocument(MultipartFile file) throws Exception {
        // 1️⃣ Sinh tên file duy nhất (tránh trùng)
        String originalName = file.getOriginalFilename();
        String uniqueName = System.currentTimeMillis() + "_" + originalName;

        // 2️⃣ Upload lên MinIO
        minioService.uploadFile(file,uniqueName);

        return DocumentResponseDTO.builder()
                .fileName(originalName)
                .path(uniqueName)
                .build();
    }

    public DocumentResponseDTO createDocument(DocumentUploadDTO documentUploadDTO){
        try{
            Contract contract = contractRepository.findById(documentUploadDTO.getContractId())
                    .orElseThrow(() -> new CustomException(ResponseCode.CONTRACT_NOT_FOUND));

            Document document = Document.builder()
                    .name(documentUploadDTO.getName())
                    .path(documentUploadDTO.getPath())        // chính là objectName trong MinIO
                    .fileName(documentUploadDTO.getFileName())
                    .bucketName(bucketName)
                    .contractId(documentUploadDTO.getContractId())
                    .type(documentUploadDTO.getType())
                    .status(documentUploadDTO.getStatus())
                    .build();

            Document savedDocument = documentRepository.save(document);

            return documentMapper.toDto(savedDocument);
        } catch (CustomException e){
            throw e;
        } catch (Exception e){
            log.error("Error creating document record: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create document record", e);
        }
    }

    public Map<String, String> getSizePage(MultipartFile file) {
        Map<String, String> result = new HashMap<>();
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            // Đọc file PDF từ MultipartFile
            PDDocument document = PDDocument.load(file.getInputStream());
            int numberOfPages = document.getNumberOfPages(); // Lấy số trang
            document.close();

            result.put("fileName", file.getOriginalFilename());
            result.put("numberOfPages", String.valueOf(numberOfPages));
            return result;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read PDF file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra xem file PDF đã có chữ ký số hay chưa
     *
     * @param file file PDF tải lên
     * @return true nếu có ít nhất 1 chữ ký số, false nếu chưa có
     */
    public boolean checkSignature(MultipartFile file) {
        log.info("Checking signature for file: {}", file.getOriginalFilename());
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            List<PDSignature> signatures = document.getSignatureDictionaries();
            return signatures != null && !signatures.isEmpty();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read PDF file: " + e.getMessage(), e);
        }
    }

    public Map<String, String> getPresignedUrl(Integer docId){
        try{
            Document document = documentRepository.findById(docId)
                    .orElseThrow(() -> new CustomException(ResponseCode.DOCUMENT_NOT_FOUND));

            String url = minioService.getPresignedUrl(document.getBucketName(), document.getPath());

            return Map.of("message", url);
        } catch (CustomException e){
            throw e;
        } catch (Exception e){
            log.error("Error generating presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    public List<DocumentResponseDTO> getDocumentByContract(Integer contractId){
       try{
           List<Document> documents = documentRepository.findByContractIdAndStatus(contractId,BaseStatus.ACTIVE.ordinal());

           return documentMapper.toDtoList(documents);
       } catch (CustomException e){
           throw e;
       } catch (Exception e){
              log.error("Error fetching documents by contract ID {}: {}", contractId, e.getMessage(), e);
              throw new RuntimeException("Failed to fetch documents by contract ID", e);
       }
    }

}
