package com.ec.contract.controller;


import com.ec.contract.repository.ContractRepository;
import com.ec.contract.service.DocumentService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/documents")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Document API", description = "Quản lý thông tin tài liệu")
public class DocumentController {

    private final ContractRepository contractRepository;
    private final DocumentService documentService;

    @Operation(summary = "Kiểm tra số lượng trang tải lên", description = "Kiểm tra số lượng trang của tài liệu PDF được tải lên.")
    @GetMapping("/get-page-size")
    public ResponseEntity<Response<?>> getPageSize(@RequestParam(name = "file") MultipartFile file) {
        return ResponseEntity.ok(
                Response.success(documentService.getSizePage(file))
        );
    }

    @Operation(summary = "Kiểm tra chữ ký số trong tài liệu", description = "Kiểm tra xem tài liệu PDF có chữ ký số hay không. Nếu có  chữ ký số, trả về true; ngược lại trả về false.")
    @GetMapping("/check-signature")
    public ResponseEntity<Response<?>> checkSignature(@RequestParam(name = "file") MultipartFile file) {

        var result = documentService.checkSignature(file);

        return ResponseEntity.ok(
                Response.success(Map.of("hasSignature", result))
        );
    }
}
