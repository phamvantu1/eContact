package com.ec.contract.controller;

import com.ec.contract.service.RecipientService;
import com.ec.library.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recipients")
@RequiredArgsConstructor
@Tag(name = "Recipient Controller", description = "APIs for managing contract recipients")
public class RecipientController {

    private final RecipientService recipientService;

    @Operation(summary = "Lấy thông tin người xử lý theo ID", description = "Lấy thông tin người xử lý theo ID")
    @GetMapping("/{recipientId}")
    public Response<?> getRecipientById(@PathVariable("recipientId") Integer recipientId){
        return Response.success(
                recipientService.getRecipientById(recipientId)
        );
    }

}
