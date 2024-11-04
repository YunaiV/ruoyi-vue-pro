package com.somle.amazon.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import com.somle.amazon.controller.vo.AmazonSpOrderReqVO;
import com.somle.amazon.model.AmazonSeller;
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

    public List<JSONObject> getOrders(LocalDateTime startTime, LocalDateTime endTime) {
        return service.spClient.getShops().map(shop -> {
            var vo = AmazonSpOrderReqVO.builder()
                    .MarketplaceIds(List.of(shop.getCountry().getMarketplaceId()))
                    .CreatedAfter(shop.getCountry().localTime(startTime))
                    .CreatedBefore(shop.getCountry().localTime(endTime))
                    .build();
            return service.spClient.getOrder(shop.getSeller(), vo);
        }).toList();
    }
}