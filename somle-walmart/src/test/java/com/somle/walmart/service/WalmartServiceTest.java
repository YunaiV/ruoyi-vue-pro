package com.somle.walmart.service;


import com.somle.framework.test.core.ut.BaseDbUnitTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Slf4j
@Import({WalmartService.class})
class WalmartServiceTest extends BaseDbUnitTest {
    @Resource
    WalmartService service;

    @Test
    void getOrders() {
        log.info(service.getClient().getOrders().toString());
    }

    @Test
    void getAvailableReconFiles() {
        log.info(service.getClient().getAvailableReconFileDates().toString());
    }

    @Test
    void getReconFile() {
        log.info(service.getClient().getAvailableReconFile("02272024"));
    }

    @Test
    void getPaymentStatement() {
        log.info(service.getClient().getPaymentStatement().toString());
    }
}