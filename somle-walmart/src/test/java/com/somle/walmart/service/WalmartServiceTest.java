package com.somle.walmart.service;


import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.walmart.model.WalmartOrderReqVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

@Disabled
@Slf4j
@Import({WalmartService.class})
class WalmartServiceTest extends SomleBaseDbUnitTest {
    @Resource
    WalmartService service;


    @Test
    void getAccessToken() {
        log.info(service.getClient().getAccessToken());
    }

    @Test
    void getOrders() {
        var start = LocalDateTime.now().minusDays(1);
        var end = LocalDateTime.now();

        var vo = WalmartOrderReqVO.builder()
                .createdStartDate(start)
                .createdEndDate(end)
                .build();
        log.info(service.getClient().getOrders(vo).toString());
    }

    @Test
    void getAvailableReconFiles() {
        log.info(service.getClient().getAvailableReconFileDates().toString());
    }

    @Test
    void getReconFile() {
        log.info(service.getClient().getReconFile("02272024"));
    }

    @Test
    void getPaymentStatement() {
        log.info(service.getClient().getPaymentStatement().toString());
    }
}