package com.ec.contract.controller;

import com.ec.contract.service.DashBoardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard Controller", description = "APIs for managing Dashboard Controller")
public class DashBoardController {

    private final DashBoardService dashBoardService;
}
