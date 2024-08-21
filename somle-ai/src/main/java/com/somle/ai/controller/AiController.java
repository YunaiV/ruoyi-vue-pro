package com.somle.ai.controller;


import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.somle.ai.model.AiName;
import com.somle.erp.model.ErpCountry;
import com.somle.ai.service.AiService;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    @Autowired
    private AiService aiService;

    // @PostMapping("/nameLookup")
    // @ResponseBody
    // public List<AiName> nameLookup( 
    //     @RequestBody List<String> nameList
    // ) {
    //     return aiService.nameLookup(nameList);
    // }

    @GetMapping("/getCountries")
    @ResponseBody
    public List<ErpCountry> getCountries(
    ) {
        return aiService.getCountries().toList();
    }

    @GetMapping("/getNames")
    @ResponseBody
    public List<AiName> getNames(
        @RequestParam String dataDate
    ) {
        return aiService.getNames(LocalDate.parse(dataDate)).toList();
    }

}