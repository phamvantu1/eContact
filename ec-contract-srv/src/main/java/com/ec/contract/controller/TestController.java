package com.ec.contract.controller;

import com.ec.contract.model.entity.Document;
import com.ec.contract.repository.DocumentRepository;
import com.ec.contract.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class TestController {

    private final MinioService minioService;
    private final DocumentRepository documentRepository;

    @PostMapping("/upload")
    public String uploadContract(@RequestParam("file") MultipartFile file) throws Exception {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        return minioService.uploadFile(file, fileName);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadContract(@PathVariable String fileName) throws Exception {
        try (InputStream inputStream = minioService.downloadFile(fileName)) {
            byte[] content = inputStream.readAllBytes();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(content);
        }
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<String> getViewUrl(@PathVariable Integer id) throws Exception {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        String url = minioService.getPresignedUrl(doc.getBucketName(), doc.getPath());

        return ResponseEntity.ok(url);
    }


}