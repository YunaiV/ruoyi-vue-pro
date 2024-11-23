package com.somle.amazon.service;

import com.somle.amazon.controller.vo.AmazonSpOrderReqVO;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO;
import com.somle.amazon.controller.vo.AmazonSpReportSaveVO;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO.ProcessingStatuses;
import com.somle.amazon.repository.AmazonAccountRepository;
import com.somle.framework.common.util.date.LocalDateTimeUtils;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.web.WebUtils;
import com.somle.framework.test.core.ut.BaseSpringTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Slf4j
@Import({
        AmazonService.class,
})
class AmazonSpClientTest extends BaseSpringTest {
    @Resource
    AmazonService amazonService;

    @Resource
    AmazonAccountRepository accountRepository;

//    @Test
//    void getSettlementReport() {
//        var shop = amazonService.shopRepository.findByCountryCode("UK");
//        var report = amazonService.spClient.getSettlementReport(shop LocalDate.of(2024,8,10));
//        log.info(report.toString());
//    }

    @Test
    void refreshToken() {
        amazonService.refreshAuth();
    }


    @Test
    void getOrder() {
        var shop = amazonService.shopRepository.findByCountryCode("UK");
        var vo = AmazonSpOrderReqVO.builder()
                .createdAfter(LocalDateTime.of(2024,10,23,0,0))
                .createdBefore(LocalDateTime.of(2024,10,24,0,0))
                .marketplaceIds(List.of(shop.getCountry().getMarketplaceId()))
                .build();
        // assert url equals "https://sellingpartnerapi-eu.amazon.com/orders/v0/orders?CreatedAfter=2024-10-23T00%3A00%3A00&CreatedBefore=2024-10-24T00%3A00%3A00&MarketplaceIds=A1F83G8C2ARO7P"
        var report = amazonService.spClient.getOrder(shop.getSeller(), vo);
        log.info(report.toString());
    }

    @Test
    void getOrder2() {
        var results = amazonService.spClient.getShops().map(shop -> {
            var startTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusMinutes(60);
            startTime = LocalDateTimeUtils.leap(startTime, ZoneId.of("UTC"));
            var vo = AmazonSpOrderReqVO.builder()
                .marketplaceIds(List.of(shop.getCountry().getMarketplaceId()))
                .createdAfter(startTime)
                .build();
            var result = amazonService.spClient.getOrder(shop.getSeller(), vo);
            log.info(shop.getCountry().getCode());
            log.info(result.toString());
            return result;
        }).toList();
    }

    @Test
    void streamOrder() {
        var shop = amazonService.shopRepository.findByCountryCode("UK");
        var vo = AmazonSpOrderReqVO.builder()
            .createdAfter(LocalDateTime.of(2024,10,23,0,0))
            .createdBefore(LocalDateTime.of(2024,10,24,0,0))
            .marketplaceIds(List.of(shop.getCountry().getMarketplaceId()))
            .build();
        // assert url equals "https://sellingpartnerapi-eu.amazon.com/orders/v0/orders?CreatedAfter=2024-10-23T00%3A00%3A00&CreatedBefore=2024-10-24T00%3A00%3A00&MarketplaceIds=A1F83G8C2ARO7P"
        var report = amazonService.spClient.streamOrder(shop.getSeller(), vo);
        log.info(report.toList().toString());
    }


    @Test
    void getAsinReport() {
        var shop = amazonService.shopRepository.findByCountryCode("UK");
        var options = AmazonSpReportSaveVO.ReportOptions.builder()
                .asinGranularity("CHILD")
                .dateGranularity("DAY")
                .build();
        var vo = AmazonSpReportSaveVO.builder()
                .reportType("GET_SALES_AND_TRAFFIC_REPORT")
                .marketplaceIds(List.of(shop.getCountry().getMarketplaceId()))
                .reportOptions(options)
                .build();
        var reportString = amazonService.spClient.createAndGetReport(shop.getSeller(), vo, "gzip");
        var report = JsonUtils.parseObject(reportString, JSONObject.class);
        log.info(report.toString());
    }

    @Test
    void getReports() {
        var shop = amazonService.shopRepository.findByCountryCode("US");
        var vo = AmazonSpReportReqVO.builder()
                .reportTypes(List.of("GET_FLAT_FILE_RETURNS_DATA_BY_RETURN_DATE"))
                .processingStatuses(List.of(ProcessingStatuses.DONE))
                .pageSize(100)
                .build();
        var reports = amazonService.spClient.getReports(shop.getSeller(), vo);
        for (var report : reports) {
            log.info(report.toString());
        }
    }

    @Test
    void getReport() {
        var shop = amazonService.shopRepository.findByCountryCode("US");
        var vo = AmazonSpReportReqVO.builder()
                .reportTypes(List.of("GET_FLAT_FILE_RETURNS_DATA_BY_RETURN_DATE"))
                .processingStatuses(List.of(ProcessingStatuses.DONE))
                .pageSize(100)
                .build();
        var report = amazonService.spClient.getReports(shop.getSeller(), vo).get(0);
        log.info(amazonService.spClient.getReport(shop.getSeller(),report.getReportId(),null));
    }


}