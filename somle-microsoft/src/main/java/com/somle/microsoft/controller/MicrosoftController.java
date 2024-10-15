package com.somle.microsoft.controller;


import java.time.LocalDate;
import java.util.List;

import com.somle.microsoft.service.MicrosoftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/ai")
public class MicrosoftController {
    @Autowired
    private MicrosoftService service;



//    @GetMapping("/getNames")
//    @ResponseBody
//    public List<AiName> getNames(@RequestParam String dataDate) {
//        return aiService.getNames(LocalDate.parse(dataDate)).toList();
//    }

}