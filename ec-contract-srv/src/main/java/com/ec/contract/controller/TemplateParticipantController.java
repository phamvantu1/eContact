package com.ec.contract.controller;

import com.ec.contract.model.dto.ParticipantDTO;
import com.ec.contract.service.TemplateParticipantService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/template-participants")
@RequiredArgsConstructor
@Tag(name = "Template Participant Controller API", description = "Quản lý thông tin hợp tổ chức tham gia hợp đồng mẫu")
public class TemplateParticipantController {

    private final TemplateParticipantService templateParticipantService;

    @Operation(summary = "Tạo tổ chức tham gia hợp đồng", description = "Tạo tổ chức tham gia hợp đồng trong hệ thống.")
    @PostMapping("/create-participant/{contractId}")
    public Response<?> createParticipant(@RequestBody List<ParticipantDTO> participantDTOList,
                                         @PathVariable("contractId") Integer contractId) {

        return Response.success(templateParticipantService.createParticipant(participantDTOList, contractId));

    }

    @GetMapping("/{participantId}")
    @Operation(summary = "Lấy thông tin tổ chức tham gia hợp đồng theo ID", description = "Lấy thông tin chi tiết của một tổ chức tham gia hợp đồng dựa trên ID.")
    public Response<?> getParticipantById(@PathVariable(name = "participantId") Integer participantId) {
        return Response.success(templateParticipantService.getParticipantById(participantId));
    }

    @GetMapping("/by-contract/{contractId}")
    @Operation(summary = "Lấy danh sách tổ chức tham gia hợp đồng theo ID hợp đồng mẫu", description = "Lấy danh sách các tổ chức tham gia hợp đồng dựa trên ID hợp đồng mẫu.")
    public Response<?> getParticipantsByContractId(@PathVariable(name = "contractId") Integer contractId) {
        return Response.success(templateParticipantService.getParticipantsByContractId(contractId));
    }

    @GetMapping("/byRecipientId/{recipientId}")
    @Operation(summary = "Lay danh sach participant theo recipientId")
    public Response<?> getByRecipientId(@PathVariable(name = "recipientId") Integer recipientId){
        return Response.success(templateParticipantService.getByRecipientId(recipientId));
    }

}
