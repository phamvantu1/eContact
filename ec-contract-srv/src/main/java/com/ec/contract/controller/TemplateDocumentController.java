package com.ec.contract.controller;

import com.ec.contract.model.dto.request.DocumentUploadDTO;
import com.ec.contract.service.TemplateDocumentService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/template-documents")
@RequiredArgsConstructor
@Tag(name = "Template Document Controller", description = "Quản lý thông tin tài liệu mẫu")
public class TemplateDocumentController {

    private final TemplateDocumentService templateDocumentService;

    @PostMapping("/create-document")
    @Operation(summary = "Tạo bản ghi tài liệu mẫu", description = "Tạo bản ghi tài liệu mâu trong hệ thống dựa trên thông tin đã tải lên.")
    public ResponseEntity<Response<?>> createTemplateDocument(@RequestBody DocumentUploadDTO documentUploadDTO) {
        return ResponseEntity.ok(
                Response.success(templateDocumentService.createTemplateDocument(documentUploadDTO))
        );
    }

    @Operation(summary = "Lấy URL truy cập tài liệu", description = "Lấy URL truy cập tạm thời cho tài liệu đã lưu trữ.")
    @GetMapping("/get-presigned-url/{docId}")
    public ResponseEntity<Response<?>> getPresignedUrl(@PathVariable(name = "docId") Integer docId) throws Exception {
        return ResponseEntity.ok(
                Response.success(templateDocumentService.getPresignedUrl(docId))
        );
    }

    @Operation(summary = "Lấy tài liệu mẫu theo hợp đồng", description = "Lấy thông tin tài liệu mẫu dựa trên ID hợp đồng.")
    @GetMapping("/get-template-document-by-contract/{contractId}")
    public ResponseEntity<Response<?>> getTemplateDocumentByContract(@PathVariable Integer contractId) {
        return ResponseEntity.ok(
                Response.success(templateDocumentService.getTemplateDocumentByContract(contractId))
        );
    }
}
