package com.ec.auth.controller;

import com.ec.library.response.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/auths")
public class test {
    @GetMapping("/{id}")
    public Response<List<Integer>> getCustomer(@PathVariable Long id) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
       return Response.success(list);
    }
}
