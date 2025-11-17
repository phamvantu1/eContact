package com.ec.contract.controller;

import com.ec.contract.model.dto.FieldDto;
import com.ec.contract.service.TemplateFieldService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/template-fields")
@RequiredArgsConstructor
@Tag(name = "Template Field Controller", description = "APIs for managing contract fields Template")
public class TemplateFieldController {

    private final TemplateFieldService templateFieldService;

    @Operation(summary = "Thêm mới danh sách trường dữ liệu cho hợp đồng", description = "Thêm mới danh sách trường dữ liệu cho hợp đồng")
    @PostMapping("/create")
    public Response<?> createFields(@Valid @RequestBody List<FieldDto> fieldDtoList){
        return  Response.success(
                templateFieldService.createFields(fieldDtoList)
        );
    }

    @GetMapping("/{fieldId}")
    @Operation(summary = "Lấy thông tin ô field")
    public Response<?> getFieldById(@PathVariable("fieldId") Integer fieldId){
        return Response.success(templateFieldService.getFieldById(fieldId));
    }

    @GetMapping("/by-contract/{contractId}")
    @Operation(summary = "Lấy thông tin ô field theo id hợp đồng mẫu")
    public Response<?> getByContract(@PathVariable("contractId") Integer contractId){
        return Response.success(templateFieldService.getByContract(contractId));
    }
}
