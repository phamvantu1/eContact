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

}
