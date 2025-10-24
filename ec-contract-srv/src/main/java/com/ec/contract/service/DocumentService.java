package com.ec.contract.service;

import com.ec.contract.model.entity.Document;
import com.ec.contract.repository.DocumentRepository;
import com.ec.library.exception.CustomException;
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

    @Value("${minio.bucket-name}")
    private String bucketName;

    public Document uploadDocument(MultipartFile file, Integer contractId, Integer type) throws Exception {
        // 1️⃣ Sinh tên file duy nhất (tránh trùng)
        String originalName = file.getOriginalFilename();
        String uniqueName = System.currentTimeMillis() + "_" + originalName;

        // 2️⃣ Upload lên MinIO
        minioService.uploadFile(file, uniqueName);

        // 3️⃣ Lưu bản ghi vào DB
        Document document = Document.builder()
                .name(originalName)
                .path(uniqueName)        // chính là objectName trong MinIO
                .fileName(originalName)
                .bucketName(bucketName)
                .contractId(contractId)
                .type(type)
                .status(1)
                .build();

        return documentRepository.save(document);
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

}
