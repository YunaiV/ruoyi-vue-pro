package com.somle.amazon.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.somle.amazon.controller.vo.AmazonSpOrderReqVO;
import com.somle.amazon.controller.vo.AmazonSpOrderRespVO;
import com.somle.framework.common.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.somle.amazon.service.AmazonService;


@RestController
@RequestMapping("/api/amazon/sp")
public class AmazonSpController {
    @Autowired
    private AmazonService service;

    @GetMapping("orders")
    public List<AmazonSpOrderRespVO> getOrders(LocalDateTime startTime) {
        return service.spClient.getShops().flatMap(shop -> {
            var vo = AmazonSpOrderReqVO.builder()
                .marketplaceIds(List.of(shop.getCountry().getMarketplaceId()))
                .createdAfter(shop.getCountry().localTime(startTime))
                .build();
            return service.spClient.streamOrder(shop.getSeller(), vo);
        }).toList();
    }
}