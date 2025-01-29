package com.somle.amazon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.somle.amazon.service.AmazonService;

@RestController
@RequestMapping("/api/amazon/ad")
public class AmazonAdController {
    @Autowired
    private AmazonService service;
}