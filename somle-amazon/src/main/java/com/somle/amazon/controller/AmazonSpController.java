package com.somle.amazon.controller;

import com.somle.amazon.service.AmazonSpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/amazonsp")
public class AmazonSpController {
    @Autowired
    private AmazonSpService service;

    @PostMapping("refreshAuth")
    void refreshAuth() {
        service.refreshAuths();
    }

//    @GetMapping("orders")
//    public List<AmazonSpOrderRespVO> getOrders(LocalDateTime startTime) {
//        return service.spClient.getShops().flatMap(shop -> {
//            var vo = AmazonSpOrderReqVO.builder()
//                .marketplaceIds(List.of(shop.getCountry().getMarketplaceId()))
//                .createdAfter(LocalDateTimeUtils.leap(startTime, ZoneId.of("UTC")))
//                .build();
//            return service.spClient.streamOrder(shop.getSeller(), vo);
//        }).toList();
//    }
}