package com.somle.dingtalk.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.somle.dingtalk.model.DingTalkDepartment;
import com.somle.dingtalk.service.DingTalkService;

@RestController
@RequestMapping("/api/dingtalk")
public class DingTalkController {
    @Autowired
    private DingTalkService service;

    @GetMapping("/getDepartments")
    public List<DingTalkDepartment> getDepartments( 
    ) {
        return service.getDepartmentStream().toList();
    }

//    @GetMapping("/saveDepartments")
//    public String uploadDepartments() {
//        service.uploadDepartmentsResursive();
//        return "success";
//    }



    @GetMapping("/cleanDepartments")
    public boolean cleanDepartments( 
    ) {
        return service.cleanDepartments();
    }
}