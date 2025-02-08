package com.somle.amazon.controller;


import com.somle.amazon.service.AmazonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/amazon/")
public class AmazonController {
    @Autowired
    private AmazonService service;

//    @PostMapping("refreshAuth")
//    void refreshAuth() {
//        service.refreshAuth();
//    }
}