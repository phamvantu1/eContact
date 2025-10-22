package com.ec.contract.controller;

import com.ec.contract.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class TestController {

    private final MinioService minioService;

    @PostMapping("/upload")
    public String uploadContract(@RequestParam("file") MultipartFile file) throws Exception {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        return minioService.uploadFile(file, fileName);
    }

    @GetMapping("/download/{fileName}")
    public byte[] downloadContract(@PathVariable String fileName) throws Exception {
        try (InputStream inputStream = minioService.downloadFile(fileName)) {
            return inputStream.readAllBytes();
        }
    }
}