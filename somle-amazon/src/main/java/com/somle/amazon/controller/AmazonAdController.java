package com.somle.amazon.controller;

import com.somle.amazon.service.AmazonAdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/amazonad")
public class AmazonAdController {
    @Autowired
    private AmazonAdService service;

    @PostMapping("refreshAuth")
    void refreshAuth() {
        service.refreshAuth();
    }

    @GetMapping("authUrl")
    String getAuthUrl() {
        return service.getAuthUrl();
    }

    @GetMapping("authUrlRedirect")
    String authUrlRedirect(@RequestParam String code, @RequestParam String scope) {
        service.createAuth(code);
        return "success";
    }
}