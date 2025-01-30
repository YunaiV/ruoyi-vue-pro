package com.somle.amazon.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import com.somle.amazon.controller.vo.AmazonSpOrderReqVO;
import com.somle.amazon.controller.vo.AmazonSpOrderRespVO;
import com.somle.framework.common.util.date.LocalDateTimeUtils;
import com.somle.framework.common.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.somle.amazon.service.AmazonService;


@RestController
@RequestMapping("/api/amazon/sp")
public class AmazonSpController {
    @Autowired
    private AmazonService service;

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