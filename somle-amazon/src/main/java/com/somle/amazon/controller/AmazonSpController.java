package com.somle.amazon.controller;

import java.time.LocalDate;

import com.somle.framework.common.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.somle.amazon.service.AmazonService;


@RestController
@RequestMapping("/api/amazon/sp")
public class AmazonSpController {
    @Autowired
    private AmazonService service;

    @GetMapping("/getAsinReport")
    @ResponseBody
    public JSONObject getAsinReport(
        @RequestParam String countryCode,
        @RequestParam LocalDate dataDate
    ) {
    
        return service.spClient.getAsinReport(countryCode, dataDate);
    }
}