package com.ec.contract.controller;

import com.ec.contract.model.dto.ContractChangeStatusRequest;
import com.ec.contract.model.dto.request.ContractRequestDTO;
import com.ec.contract.service.TemplateContractService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/template-contracts")
@RequiredArgsConstructor
@Tag(name = "Template Contract Controller", description = "Quản lý thông tin hợp đồng mẫu")
public class TemplateContractController {

    private final TemplateContractService templateContractService;

    @PostMapping("/create-contract")
    @Operation(summary = "Tạo hợp đồng mới", description = "Tạo một hợp đồng mới trong hệ thống.")
    public Response<?> createContract(@RequestBody ContractRequestDTO contractRequestDTO,
                                      Authentication authentication) {
        return Response.success(templateContractService.createTemplateContract(contractRequestDTO, authentication));
    }

    @PutMapping("{contractId}/change-status/{new-status}")
    @Operation(summary = "Thay đổi trạng thái hợp đồng mẫu", description = "Cập nhật trạng thái của một hợp đồng mẫu dựa trên ID hợp đồng và trạng thái mới.")
    public Response<?> changeContractStatus(@PathVariable(name = "contractId") Integer contractId,
                                            @PathVariable(name = "new-status") Integer status,
                                            @RequestBody @Valid Optional<ContractChangeStatusRequest> request) {
        return Response.success(templateContractService.changeContractStatus(contractId, status, request));
    }

}
