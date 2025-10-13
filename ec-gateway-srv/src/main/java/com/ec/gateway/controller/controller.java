package com.ec.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class controller {

    @GetMapping("/xin-chao")
    public String hello() {
        System.out.println("Hello, World!");
        return "Hello, World!";
    }
}
