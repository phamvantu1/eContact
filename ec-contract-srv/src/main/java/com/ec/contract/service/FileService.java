package com.ec.contract.service;

import com.ec.contract.constant.DocumentType;
import com.ec.contract.model.dto.response.DocumentResponseDTO;
import com.ec.contract.model.entity.Contract;
import com.ec.contract.model.entity.Document;
import com.ec.contract.model.entity.TemplateDocument;
import com.ec.contract.repository.ContractRepository;
import com.ec.contract.repository.DocumentRepository;
import com.ec.contract.repository.TemplateDocumentRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final DocumentService documentService;
    private final DocumentRepository documentRepository;
    private final ContractRepository contractRepository;
    private final TemplateDocumentRepository templateDocumentRepository;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public String replace(String newFilePath, Integer docId) throws Exception {
        try{
            log.info(String.format("start replace file <- %s",  newFilePath));
            log.info("Request replace file trên minio , path file cũ : {} , ", newFilePath);

            Document document = documentRepository.findById(docId).orElseThrow(() -> new CustomException(ResponseCode.DOCUMENT_NOT_FOUND));

            final var headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            File file = new File(newFilePath);
            FileInputStream input = new FileInputStream(file);

            MultipartFile multipartFile = new MockMultipartFile(
                    document.getName(),                  // tên field form
                    file.getName(),          // tên file gốc
                    "application/pdf",       // ✅ MIME type cho PDF
                    input                    // dữ liệu file
            );

            // 2️⃣ Gọi hàm uploadDocument() (bạn đã có sẵn)
            DocumentResponseDTO uploadResponse = documentService.uploadDocument(multipartFile);

            log.info("Response trả về request replace file trên Minio : {}", uploadResponse);

            if(document != null){

                Document newDocument = Document.builder()
                        .name(document.getName())
                        .status(document.getStatus())
                        .type(DocumentType.FINALLY.getDbVal())
                        .contractId(document.getContractId())
                        .fileName(uploadResponse.getFileName())
                        .path(uploadResponse.getPath())
                        .bucketName(document.getBucketName())
                        .build();

                documentRepository.save(newDocument);
            }

            return "success";
        }catch (Exception e){
            log.error("Error replacing file in MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to replace file in MinIO", e);
        }
    }

    public String replaceTemplate(String newFilePath, Integer docId) throws Exception {
        try{
            log.info(String.format("start replaceTemplate file <- %s",  newFilePath));
            log.info("Request replaceTemplate file trên minio , path file cũ : {} , ", newFilePath);

            TemplateDocument document = templateDocumentRepository.findById(docId).orElseThrow(() -> new CustomException(ResponseCode.DOCUMENT_NOT_FOUND));

            final var headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            File file = new File(newFilePath);
            FileInputStream input = new FileInputStream(file);

            MultipartFile multipartFile = new MockMultipartFile(
                    document.getName(),                  // tên field form
                    file.getName(),          // tên file gốc
                    "application/pdf",       // ✅ MIME type cho PDF
                    input                    // dữ liệu file
            );

            // 2️⃣ Gọi hàm uploadDocument() (bạn đã có sẵn)
            DocumentResponseDTO uploadResponse = documentService.uploadDocument(multipartFile);

            log.info("Response trả về request replace file trên Minio : {}", uploadResponse);

            if(document != null){

                TemplateDocument newDocument = TemplateDocument.builder()
                        .name(document.getName())
                        .status(document.getStatus())
                        .type(DocumentType.FINALLY.getDbVal())
                        .contractId(document.getContractId())
                        .fileName(uploadResponse.getFileName())
                        .path(uploadResponse.getPath())
                        .bucketName(document.getBucketName())
                        .build();

                templateDocumentRepository.save(newDocument);
            }

            return "success";
        }catch (Exception e){
            log.error("Error replacing file in MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to replace file in MinIO", e);
        }
    }

    public boolean backUpFileMinioIfErrorAction(String pathFileReplace, Integer contractId) {
        try {
            log.info("BẮT ĐẦU BACKUP FILE LÊN MINIO");

            Document document = documentRepository.findByContractIdAndType(contractId, DocumentType.PRIMARY.getDbVal());

            if (!StringUtils.hasText(pathFileReplace)) {
                log.info("Dừng backup file dữ liệu pathFileReplace , bucketMinio , pathMinio trống");
                return true;
            }

            var res = replace(pathFileReplace, document.getId());

            if (res != null) {
                log.info("Backup file lên Minio thành công");
                return true;
            }
            log.error("Backup file lên Minio thất bại");
        } catch (Exception e) {
            log.error("Backup file lên Minio thất bại", e);
        }
        return false;
    }

}
