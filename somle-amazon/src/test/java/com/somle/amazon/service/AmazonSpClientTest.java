package com.somle.amazon.service;

import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.test.core.ut.SomleBaseSpringTest;
import com.somle.amazon.controller.vo.AmazonSpListingReqVO;
import com.somle.amazon.controller.vo.AmazonSpOrderReqVO;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO.ProcessingStatuses;
import com.somle.amazon.controller.vo.AmazonSpReportSaveVO;
import com.somle.amazon.model.enums.AmazonCountry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;


@Disabled
@Slf4j
@Import({
    AmazonService.class,
    AmazonSpService.class,
})
class AmazonSpClientTest extends SomleBaseSpringTest {
    @Resource
    AmazonSpService spService;

    private AmazonSpClient client;

    @BeforeEach
    void setUp() {
        client = spService.clients.get(1);
    }

//    @Test
//    void getSettlementReport() {
//        var shop = amazonService.shopRepository.findByCountryCode("UK");
//        var report = amazonService.spClient.getSettlementReport(shop LocalDate.of(2024,8,10));
//        log.info(report.toString());
//    }

//    @Test
//    void getAccount() {
//        var shop = amazonService.shopRepository.findByCountryCode("US");
//        var response = amazonService.spClient.getAccount(shop.getSeller());
//        log.info(response.toString());
//    }

    @Test
    void getMarketplaceParticipations() {
        var response = client.getMarketplaceParticipations();
        log.info(response.toString());
    }

    @Test
    void getListing() {
        var reqVO = AmazonSpListingReqVO.builder()
            .sellerId(client.getAuth().getSellerId())
            .marketplaceIds(List.of(AmazonCountry.findByCode("DE").getMarketplaceId()))
            .includedData(List.of(AmazonSpListingReqVO.IncludedData.OFFERS))
            .build();
        var listing = client.searchListingsItems(reqVO);
        log.info(listing);
    }


    @Test
    void getOrder() {
        var vo = AmazonSpOrderReqVO.builder()
            .createdAfter(LocalDateTime.of(2024, 10, 23, 0, 0))
            .createdBefore(LocalDateTime.of(2024, 10, 24, 0, 0))
            .marketplaceIds(List.of(AmazonCountry.findByCode("DE").getMarketplaceId()))
            .build();
        // assert url equals "https://sellingpartnerapi-eu.amazon.com/orders/v0/orders?CreatedAfter=2024-10-23T00%3A00%3A00&CreatedBefore=2024-10-24T00%3A00%3A00&MarketplaceIds=A1F83G8C2ARO7P"
        var report = client.getOrder(vo);
        log.info(report.toString());
    }

    //    @Test
//    void getOrder2() {
//        var results = amazonService.spClient.getShops().map(shop -> {
//            var startTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusMinutes(60);
//            startTime = LocalDateTimeUtils.leap(startTime, ZoneId.of("UTC"));
//            var vo = AmazonSpOrderReqVO.builder()
//                .marketplaceIds(List.of(shop.getCountry().getMarketplaceId()))
//                .createdAfter(startTime)
//                .build();
//            var result = amazonService.spClient.getOrder(shop.getSeller(), vo);
//            log.info(shop.getCountry().getCode());
//            log.info(result.toString());
//            return result;
//        }).toList();
//    }
//
//    @Test
//    void streamOrder() {
//        var shop = amazonService.shopRepository.findByCountryCode("UK");
//        var vo = AmazonSpOrderReqVO.builder()
//            .createdAfter(LocalDateTime.of(2024,10,23,0,0))
//            .createdBefore(LocalDateTime.of(2024,10,24,0,0))
//            .marketplaceIds(List.of(shop.getCountry().getMarketplaceId()))
//            .build();
//        // assert url equals "https://sellingpartnerapi-eu.amazon.com/orders/v0/orders?CreatedAfter=2024-10-23T00%3A00%3A00&CreatedBefore=2024-10-24T00%3A00%3A00&MarketplaceIds=A1F83G8C2ARO7P"
//        var report = amazonService.spClient.streamOrder(shop.getSeller(), vo);
//        log.info(report.toList().toString());
//    }
//
//
    @Test
    void getAsinReport() {
        var options = AmazonSpReportSaveVO.ReportOptions.builder()
            .asinGranularity("CHILD")
            .dateGranularity("DAY")
            .build();
        var vo = AmazonSpReportSaveVO.builder()
            .reportType("GET_SALES_AND_TRAFFIC_REPORT")
            .marketplaceIds(List.of(AmazonCountry.findByCode("DE").getMarketplaceId()))
            .reportOptions(options)
            .build();
        var reportString = client.createAndGetReport(vo);
        var report = JsonUtilsX.parseObject(reportString, JSONObject.class);
        log.info(report.toString());
    }

    @Test
    void getStorageReport() {
//        var options = AmazonSpReportSaveVO.ReportOptions.builder()
//            .asinGranularity("CHILD")
//            .dateGranularity("DAY")
//            .build();
        var vo = AmazonSpReportSaveVO.builder()
            .reportType("GET_FBA_STORAGE_FEE_CHARGES_DATA")
            .marketplaceIds(List.of(AmazonCountry.findByCode("CA").getMarketplaceId()))
            .dataStartTime(LocalDateTime.of(2024, 12, 1, 0, 0).toString())
            .dataEndTime(LocalDateTime.of(2025, 1, 2, 0, 0).toString())
//            .marketplaceIds(List.of(AmazonCountry.findByCode("DE").getMarketplaceId()))
//            .reportOptions(options)
            .build();
        var reportString = client.createAndGetReport(vo);
        log.info(reportString);
    }

    @Test
    void listReports() {
        var vo = AmazonSpReportReqVO.builder()
            .reportTypes(List.of("GET_FBA_STORAGE_FEE_CHARGES_DATA"))
            .processingStatuses(List.of(ProcessingStatuses.DONE))
            .pageSize(100)
            .build();
        var reports = client.listReports(vo);
        for (var report : reports) {
            log.info(report.toString());
        }
    }

    @Test
    void getReport() {
//        var vo = AmazonSpReportReqVO.builder()
//                .reportTypes(List.of("GET_FLAT_FILE_RETURNS_DATA_BY_RETURN_DATE"))
//                .processingStatuses(List.of(ProcessingStatuses.DONE))
//                .pageSize(100)
//                .build();
//        var report = client.getReports(vo).get(0);
        log.info(client.waitAndGetReportDocumentString("970685020124"));
    }

    @Test
    void getReportDocument() {
        var respVO = client.getReport("970685020124");
        log.info(respVO.toString());
        var listing = client.getReportDocument(respVO.getReportDocumentId());
        log.info(listing.toString());
    }


}