package com.ec.contract.controller;

import com.ec.contract.model.dto.ContractChangeStatusRequest;
import com.ec.contract.model.dto.RecipientDTO;
import com.ec.contract.model.dto.keystoreDTO.CertificateDtoRequest;
import com.ec.contract.model.dto.request.AuthorizeDTO;
import com.ec.contract.service.*;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/processes")
@AllArgsConstructor
@Slf4j
@Tag(name = "Process Controller", description = "Quản lý luồng xử lý hợp đồng")
public class ProcessController {
    private final ProcessService processService;
    private final SignService signService;

    @PutMapping("/coordinator/{participantId}/{recipientId}")
    @Operation(summary = "Điều phối hợp đồng")
    public Response<?> coordinator(
            Authentication authentication,
            @PathVariable("participantId") int participantId,
            @PathVariable("recipientId") int recipientId,
            @RequestBody @Valid Collection<RecipientDTO> recipientDtoCollection) {
        final var participantDtoOptional = processService.updateRecipientForCoordinator(
                authentication,
                participantId,
                recipientId,
                recipientDtoCollection
        );

        return Response.success(participantDtoOptional);
    }

    /**
     * Khách hàng xác nhận đồng ý xử lý hồ sơ
     *
     * @param recipientId Mã số tham chiếu khách hàng xử lý hồ sơ
     * @return Thông báo cho người dùng cuối
     */
    @Operation(summary = "Phê duyệt hợp đồng")
    @PutMapping("/approval/{recipientId}")
    public Response<?> approval(@PathVariable("recipientId") int recipientId) {

        //  gọi hàm updateFieldsTextAndCurrency để add ô text/currency của người tạo chưa được add vào hợp đồng
        //  fieldService.updateFieldsTextAndCurrency(recipientId);

        return Response.success(processService.approval(recipientId));

    }

    @Operation(summary = "Ký hợp đồng")
    @PostMapping("/certificate")
    public ResponseEntity<?> KeystoreSignFile(
            Authentication authentication,
            @RequestParam("recipientId") int recipientId,
            @Valid @RequestBody CertificateDtoRequest certificateDtoRequest) {
        certificateDtoRequest.setEmail(authentication.getName());

        final var response = signService.SignKeystore(recipientId, certificateDtoRequest);
//        if (response.isSuccess()) {
//            documentService.saveDocumentHistoryByRecipientId(recipientId);
//        }
//        signService.changEndContractProcessedByRecipientId(recipientId);
        return ResponseEntity.ok(response);

    }

    @PutMapping("/reject/{recipientId}")
    @Operation(summary = "Từ chối xử lý hợp đồng")
    public Response<?> rejectContract(
            @PathVariable("recipientId") int recipientId,
            @RequestBody ContractChangeStatusRequest reason) {

        return Response.success(processService.rejectContract(recipientId, reason));

    }

    @PostMapping("/authorize/{recipientId}")
    @Operation(summary = "Ủy quyền xử lý hợp đồng")
    public Response<?> authorizeContract(
            @PathVariable("recipientId") int recipientId,
            @RequestBody AuthorizeDTO authorizeDTO) {

        return Response.success(processService.authorizeContract(recipientId, authorizeDTO));

    }
}
