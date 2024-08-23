package com.somle.amazon.controller;

import java.time.LocalDate;

import com.somle.framework.common.util.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.somle.amazon.service.AmazonAdClient;
import com.somle.amazon.service.AmazonService;

@RestController
@RequestMapping("/api/amazon/ad")
public class AmazonAdController {
    @Autowired
    private AmazonService service;

    @GetMapping("/getAdReport")
    @ResponseBody
    public JSONArray getAdReport(
        @RequestParam String countryCode,
        @RequestParam LocalDate dataDate
    ) {
    
        return service.adClient.getAdReport(countryCode, dataDate);
    }
}