package com.somle.cdiscount.controller;

import com.somle.cdiscount.model.req.OrderReqVO;
import com.somle.cdiscount.model.resp.CdiscountOrderRespVO;
import com.somle.cdiscount.service.CdiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cdiscount")
public class CdiscountController {
    @Autowired
    CdiscountService service;

    @GetMapping("/orders")
    public CdiscountOrderRespVO orders() {
        var vo = OrderReqVO.builder()
            .pageIndex(1)
            .pageSize(100)
            .build();
        return service.clients.get(0).getOrders(vo);
    }
}