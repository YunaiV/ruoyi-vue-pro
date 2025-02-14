package com.somle.amazon.service;

import com.somle.framework.test.core.ut.BaseSpringTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;


@Slf4j
@Import({
    AmazonService.class,
    AmazonSpService.class,
})
class AmazonSpServiceTest extends BaseSpringTest {
    @Resource
    AmazonSpService spService;


//    @Test
//    void getSettlementReport() {
//        var shop = amazonService.shopRepository.findByCountryCode("UK");
//        var report = amazonService.spClient.getSettlementReport(shop LocalDate.of(2024,8,10));
//        log.info(report.toString());
//    }

    @Test
    void refreshToken() {
        spService.refreshAuths();
    }

//    @Test
//    void getAccount() {
//        var shop = amazonService.shopRepository.findByCountryCode("US");
//        var response = amazonService.spClient.getAccount(shop.getSeller());
//        log.info(response.toString());
//    }


}