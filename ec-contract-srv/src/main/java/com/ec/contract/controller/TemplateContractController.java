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

    @GetMapping("/check-code-unique")
    @Operation(summary = "Kiểm tra mã hợp đồng có duy nhất hay không", description = "Kiểm tra mã hợp đồng có duy nhất hay không trong hệ thống." +
            " Nếu mã hợp đồng đã tồn tại, trả về false; ngược lại trả về true.")
    public Response<?> checkCodeUnique(@RequestParam(name = "code") String code) {
        return Response.success(templateContractService.checkCodeUnique(code));
    }

    @DeleteMapping("/{contractId}")
    @Operation(summary = "Xóa hợp đồng mẫu", description = "Xóa một hợp đồng mẫu dựa trên ID hợp đồng.")
    public Response<?> deleteTemplateContract(@PathVariable(name = "contractId") Integer contractId) {
        var result = templateContractService.deleteTemplateContract(contractId);
        return Response.success(result);
    }

    @GetMapping("/{contractId}")
    @Operation(summary = " Lấy thông tin hợp đồng mẫu theo ID", description = "Lấy thông tin chi tiết của một hợp đồng mẫu dựa trên ID hợp đồng.")
    public Response<?> getTemplateContractById(@PathVariable(name = "contractId") Integer contractId) {
        return Response.success(templateContractService.getTemplateContractById(contractId));
    }

    @GetMapping("/my-contracts")
    @Operation(summary = "Danh sách hợp đồng mẫu mình đã tạo", description = "Danh sách hợp đồng mẫu mình đã tạo")
    public Response<?> getMyContracts(Authentication authentication,
                                      @RequestParam(name = "type", required = false) Integer type,
                                      @RequestParam(name = "name", required = false) String name,
                                      @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
                                      @RequestParam(name = "page", required = false, defaultValue = "0") Integer page) {
        return Response.success(templateContractService.getMyTemplateContracts(authentication, type, name, size,page));
    }

    @PutMapping("/{contractId}")
    @Operation(summary = "Cập nhật hợp đồng mẫu", description = "Cập nhật một hợp đồng mẫu trong hệ thống.")
    public Response<?> updateTemplateContract(@PathVariable(name = "contractId") Integer contractId,
                                              @RequestBody ContractRequestDTO contractRequestDTO,
                                              Authentication authentication) {
        return Response.success(templateContractService.updateTemplateContract(contractId, contractRequestDTO, authentication));
    }

}
