package com.somle.microsoft.controller;


import java.time.LocalDate;
import java.util.List;

import com.somle.microsoft.service.MicrosoftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin-api/microsoft")
public class MicrosoftController {
    @Autowired
    private MicrosoftService service;



    @GetMapping("/getPasswordToken")
    @ResponseBody
    public String getPasswordToken() {
        return service.getPasswordToken();
    }

}