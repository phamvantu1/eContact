package com.ec.contract.controller;

import com.ec.contract.model.dto.FieldDto;
import com.ec.contract.service.TemplateFieldService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
