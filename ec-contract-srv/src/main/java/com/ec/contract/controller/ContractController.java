package com.ec.contract.controller;

import com.ec.contract.model.dto.request.ContractRequestDTO;
import com.ec.contract.model.dto.request.FilterContractDTO;
import com.ec.contract.service.ContractService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Contract Controller", description = "Quản lý thông tin hợp đồng")
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/check-code-unique")
    @Operation(summary = "Kiểm tra mã hợp đồng có duy nhất hay không", description = "Kiểm tra mã hợp đồng có duy nhất hay không trong hệ thống." +
            " Nếu mã hợp đồng đã tồn tại, trả về false; ngược lại trả về true.")
    public Response<?> checkCodeUnique(@RequestParam(name = "code") String code) {
        return Response.success(contractService.checkCodeUnique(code));
    }

    @PostMapping("/create-contract")
    @Operation(summary = "Tạo hợp đồng mới", description = "Tạo một hợp đồng mới trong hệ thống.")
    public Response<?> createContract(@RequestBody ContractRequestDTO contractRequestDTO,
                                      Authentication authentication) {
        return Response.success(contractService.createContract(contractRequestDTO, authentication));
    }

    @GetMapping("/{contractId}")
    @Operation(summary = "Lấy thông tin hợp đồng theo ID", description = "Lấy thông tin chi tiết của một hợp đồng dựa trên ID hợp đồng.")
    public Response<?> getContractById(@PathVariable(name = "contractId") Integer contractId) {
        return Response.success(contractService.getContractById(contractId));
    }

    @PutMapping("/change-status/{contractId}")
    @Operation(summary = "Thay đổi trạng thái hợp đồng", description = "Cập nhật trạng thái của một hợp đồng dựa trên ID hợp đồng và trạng thái mới.")
    public Response<?> changeContractStatus(@PathVariable(name = "contractId") Integer contractId,
                                            @RequestParam(name = "status") Integer status) {
        return Response.success(contractService.changeContractStatus(contractId, status));
    }

    @PostMapping("/my-contracts")
    @Operation(summary = "Danh sách hợp đồng mình đã tạo", description = "Danh sách hợp đồng mình đã tạo")
    public Response<?> getMyContracts(Authentication authentication,
                                      @RequestBody FilterContractDTO filterContractDTO) {
        return Response.success(contractService.getMyContracts(authentication, filterContractDTO));
    }

    @PostMapping("/my-process")
    @Operation(summary = "Danh sách hợp đồng mình tham gia xử lý",
            description = "Danh sách hợp đồng mình tham gia xử lý, status 1 thì là chờ xử lý, 2 là đã xử lý")
    public Response<?> getMyProcessContracts(Authentication authentication,
                                             @RequestBody FilterContractDTO filterContractDTO) {
        return Response.success(contractService.getMyProcessContracts(authentication, filterContractDTO));
    }

    @PostMapping("/contract-by-organization")
    @Operation(summary = "Danh sách hợp đồng theo tổ chức", description = "Danh sách hợp đồng theo tổ chức")
    public Response<?> getContractsByOrganization(@RequestBody FilterContractDTO filterContractDTO) {
        return Response.success(contractService.getContractsByOrganization(filterContractDTO));
    }

    @PutMapping("/update-contract/{contractId}")
    @Operation(summary = "Cập nhật hợp đồng", description = "Cập nhật thông tin hợp đồng dựa trên ID hợp đồng.")
    public Response<?> updateContract(@PathVariable(name = "contractId") Integer contractId,
                                      @RequestBody ContractRequestDTO contractRequestDTO) {
        return Response.success(contractService.updateContract(contractId, contractRequestDTO));
    }

    @GetMapping("/bpmn-flow/{contractId}")
    @Operation(summary = "Lấy luồng BPMN của hợp đồng theo ID", description = "Lấy thông tin luồng BPMN của một hợp đồng dựa trên ID hợp đồng.")
    public Response<?> getBpmnFlowByContractId(@PathVariable(name = "contractId") Integer contractId) {
        return Response.success(contractService.getBpmnFlowByContractId(contractId));
    }


}
