package com.somle.ai.controller;


import java.time.LocalDate;
import java.util.List;

import com.somle.ai.model.AiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.somle.ai.model.AiName;
import com.somle.ai.service.AiService;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    @Autowired
    private AiService aiService;

}