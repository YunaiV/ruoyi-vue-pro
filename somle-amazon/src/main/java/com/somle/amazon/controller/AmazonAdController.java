package com.somle.amazon.controller;

import com.somle.amazon.model.enums.AmazonRegion;
import com.somle.amazon.service.AmazonAdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/amazonad")
public class AmazonAdController {
    @Autowired
    private AmazonAdService service;

    @PostMapping("refreshAuths")
    void refreshAuths() {
        service.refreshAuths();
    }

    @GetMapping("authUrl")
    String getAuthUrl() {
        return service.getAuthUrl();
    }

    @PostMapping("auth")
    String createAuth(@RequestParam String code) {
        service.createAuth(code);
        return "success";
    }
}