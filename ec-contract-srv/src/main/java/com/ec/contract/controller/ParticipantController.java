package com.ec.contract.controller;

import com.ec.contract.model.dto.ParticipantDTO;
import com.ec.contract.service.ParticipantService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/participants")
@RequiredArgsConstructor
@Tag(name = "Participant API", description = "Quản lý thông tin tổ chức tham gia hợp đồng")
public class ParticipantController {

    private final ParticipantService participantService;

    @Operation(summary = "Tạo tổ chức tham gia hợp đồng", description = "Tạo tổ chức tham gia hợp đồng trong hệ thống.")
    @PostMapping("/create-participant/{contractId}")
    public Response<?> createParticipant(@RequestBody List<ParticipantDTO> participantDTOList,
                                         @PathVariable("contractId") Integer contractId) {

        return Response.success(participantService.createParticipant(participantDTOList, contractId));

    }

    @GetMapping("/{participantId}")
    @Operation(summary = "Lấy thông tin tổ chức tham gia hợp đồng theo ID", description = "Lấy thông tin chi tiết của một tổ chức tham gia hợp đồng dựa trên ID.")
    public Response<?> getParticipantById(@PathVariable(name = "participantId") Integer participantId) {
        return Response.success(participantService.getParticipantById(participantId));
    }

    @GetMapping("/by-contract/{contractId}")
    @Operation(summary = "Lấy danh sách tổ chức tham gia hợp đồng theo ID hợp đồng", description = "Lấy danh sách các tổ chức tham gia hợp đồng dựa trên ID hợp đồng.")
    public Response<?> getParticipantsByContractId(@PathVariable(name = "contractId") Integer contractId) {
        return Response.success(participantService.getParticipantsByContractId(contractId));
    }
}
