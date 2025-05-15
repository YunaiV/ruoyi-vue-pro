package com.somle.amazon.service;

import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.test.core.ut.SomleBaseSpringTest;
import com.somle.amazon.controller.vo.*;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO.ProcessingStatuses;
import com.somle.amazon.model.enums.AmazonCountry;
import com.somle.amazon.model.reps.AmazonSpListingRepsVO;
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
        client = spService.clients.get(2);
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
    void getListing() {
        AmazonSpMarketplaceParticipationVO marketplaceParticipations = client.getMarketplaceParticipations().get(0);
        List<AmazonSpListingRepsVO.ProductItem> products = client.getProducts(List.of(marketplaceParticipations.getMarketplace().getId()));
        log.info(products.toString());
    }


    @Test
    void getOrder() {
        AmazonSpMarketplaceParticipationVO marketplaceParticipations = client.getMarketplaceParticipations().get(0);
        var vo = AmazonSpOrderReqVO.builder()
            .createdAfter(LocalDateTime.of(2025, 05, 12, 0, 0, 0))
            .createdBefore(LocalDateTime.of(2025, 05, 12, 23, 23, 23))
            .marketplaceIds(List.of(marketplaceParticipations.getMarketplace().getId()))
            .build();
        // assert url equals "https://sellingpartnerapi-eu.amazon.com/orders/v0/orders?CreatedAfter=2024-10-23T00%3A00%3A00&CreatedBefore=2024-10-24T00%3A00%3A00&MarketplaceIds=A1F83G8C2ARO7P"
        var report = client.getOrder(vo);
        List<AmazonSpOrderRespVO> list = client.streamOrder(vo).toList();
        log.info(report.toString());
    }

    @Test
    void getOrderBuyerInfo() {
        var report = client.getOrderBuyerInfo("701-5221425-3625846");
        log.info(report);
    }

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
            .pageSize(20)
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