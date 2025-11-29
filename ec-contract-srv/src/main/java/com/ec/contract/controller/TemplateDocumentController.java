package com.ec.contract.controller;

import com.ec.contract.model.dto.request.DocumentUploadDTO;
import com.ec.contract.service.DocumentService;
import com.ec.contract.service.TemplateDocumentService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/template-documents")
@RequiredArgsConstructor
@Tag(name = "Template Document Controller", description = "Quản lý thông tin tài liệu mẫu")
public class TemplateDocumentController {

    private final TemplateDocumentService templateDocumentService;
    private final DocumentService documentService;

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

    @Operation(summary = "Kiểm tra số lượng trang tải lên", description = "Kiểm tra số lượng trang của tài liệu PDF được tải lên.")
    @PostMapping("/get-page-size")
    public ResponseEntity<Response<?>> getPageSize(@RequestParam(name = "file") MultipartFile file) {
        return ResponseEntity.ok(
                Response.success(documentService.getSizePage(file))
        );
    }

    @Operation(summary = "Kiểm tra chữ ký số trong tài liệu", description = "Kiểm tra xem tài liệu PDF có chữ ký số hay không. Nếu có  chữ ký số, trả về true; ngược lại trả về false.")
    @PostMapping("/check-signature")
    public ResponseEntity<Response<?>> checkSignature(@RequestParam(name = "file") MultipartFile file) {

        var result = documentService.checkSignature(file);

        return ResponseEntity.ok(
                Response.success(Map.of("hasSignature", result))
        );
    }

    @Operation(summary = "Tải lên tài liệu", description = "Tải lên tài liệu lên hệ thống.")
    @PostMapping("/upload-document")
    public ResponseEntity<Response<?>> uploadDocument(@RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(
                Response.success(documentService.uploadDocument(file))
        );
    }

    @PostMapping("/verify-signature")
    @Operation(summary = "Xác minh chữ ký số trong tài liệu", description = "Xác minh chữ ký số trong tài liệu PDF và trả về kết quả chi tiết về chữ ký.")
    public Response<?> verifyPdf(@RequestParam("file") MultipartFile file) {
        return Response.success(documentService.verifyPdfSignature(file));
    }

}
