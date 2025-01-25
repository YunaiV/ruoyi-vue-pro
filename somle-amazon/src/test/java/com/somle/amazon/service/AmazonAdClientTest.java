package com.somle.amazon.service;

import com.somle.amazon.controller.vo.AmazonSpOrderReqVO;
import com.somle.framework.test.core.ut.BaseSpringTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Import({
    AmazonService.class,
})
class AmazonAdClientTest extends BaseSpringTest {
    @Resource
    AmazonService amazonService;

//    @Test
//    void getShops() {
//    }
//
//    @Test
//    void getShop() {
//    }
//
//    @Test
//    void getAllAdReport() {
//    }

    @Test
    void listProfiles() {
        var seller = amazonService.shopRepository.findByCountryCode("UK").getSeller();
        var response = amazonService.adClient.listProfiles(seller);
        log.info(response.toString());
    }

    @Test
    void getAdReport() {
        var shop = amazonService.shopRepository.findByCountryCode("UK");
        var report = amazonService.adClient.listPortfolios(shop);
        log.info(report.toString());
    }

//    @Test
//    void testGetAdReport() {
//    }
//
//    @Test
//    void getReport() {
//    }
}