package com.somle.amazon.service;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseSpringTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;


@Disabled
@Slf4j
@Import({
    AmazonService.class,
    AmazonSpService.class,
})
class AmazonSpServiceTest extends SomleBaseSpringTest {
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

    @Test
    void getAccount() {
        spService.clients.forEach(client -> {
            log.info("{}", client.getMarketplaceParticipations());
        });
    }


}