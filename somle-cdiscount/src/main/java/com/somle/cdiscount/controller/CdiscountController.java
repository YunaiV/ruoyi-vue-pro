package com.somle.cdiscount.controller;

import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import com.somle.cdiscount.service.CdiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cdiscount")
public class CdiscountController {
    @Autowired
    CdiscountService service;

    @GetMapping("/orders")
    public JSONObject orders() {
        return service.client.getOrders();
    }
}