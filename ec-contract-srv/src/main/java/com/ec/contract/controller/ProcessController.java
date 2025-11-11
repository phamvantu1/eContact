package com.ec.contract.controller;

import com.ec.contract.model.dto.RecipientDTO;
import com.ec.contract.service.*;
import com.ec.library.response.Response;
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
public class ProcessController {
    private final ProcessService processService;

    @PutMapping("/coordinator/{participantId}/{recipientId}")
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

        return Response.success(participantDtoOptional.orElse(null));
    }

    /**
     * Khách hàng xác nhận đồng ý xử lý hồ sơ
     *
     * @param recipientId                  Mã số tham chiếu khách hàng xử lý hồ sơ
     * @return Thông báo cho người dùng cuối
     */
    @PutMapping("/approval/{recipientId}")
    public Response<?> approval(@PathVariable("recipientId") int recipientId) {

        //  gọi hàm updateFieldsTextAndCurrency để add ô text/currency của người tạo chưa được add vào hợp đồng

//        fieldService.updateFieldsTextAndCurrency(recipientId);

        return Response.success(processService.approval(recipientId));

    }

    @PostMapping("/certificate")
    public ResponseEntity<MessageDto> KeystoreSignFile(
            @CurrentCustomer CustomerUser customerUser,
            @RequestParam("id") int recipientId,
            @Valid @RequestBody CertificateDtoRequest certificateDtoRequest) {
        certificateDtoRequest.setEmail(customerUser.getEmail());
        certificateDtoRequest.setPhone(customerUser.getPhone());
        final var response = signService.SignKeystore(recipientId, certificateDtoRequest);
        if (response.isSuccess()) {
            documentService.saveDocumentHistoryByRecipientId(recipientId);
        }
        signService.changEndContractProcessedByRecipientId(recipientId);
        return ResponseEntity.ok(response);

    }


}
