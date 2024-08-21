package com.somle.amazon.service;

import com.somle.framework.test.core.ut.BaseSpringTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;


@Slf4j
@Import({
    AmazonService.class,
})
class AmazonServiceTest extends BaseSpringTest {
    @Resource
    AmazonService amazonService;


    @Test
    void refreshAuth() {
        amazonService.refreshAuth();
    }

    @Test
    void getAsinReport() {
        var report = amazonService.spClient.getAsinReport("UK", LocalDate.of(2024,8,10));
        log.info(report.toString());
    }
}