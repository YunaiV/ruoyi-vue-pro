package com.somle.home24.service;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.home24.model.req.Home24InvoicesReq;
import com.somle.home24.model.req.Home24OrderReq;
import com.somle.home24.model.resp.Home24CommonInvoicesResp;
import com.somle.home24.model.resp.Home24CommonOrdersResp;
import com.somle.home24.repository.Home24AccountRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;

@Slf4j
class Home24ClientTest extends SomleBaseDbUnitTest {
    @Resource
    Home24AccountRepository home24AccountRepository;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @Rollback(value = false)
    void getOrder() {
        home24AccountRepository.findAll()
            .stream()
            .findFirst()
            .ifPresent(home24Account -> {
                Home24Client client = new Home24Client(home24Account);

                Home24OrderReq req = Home24OrderReq.builder()
                    .max(100)
                    .startDate("2024-12-01T14:30:00Z")
                    .endDate("2024-12-24T14:30:00Z")
                    .build();

                Home24CommonOrdersResp<Object> commonRespOrders = client.getOrder(req);
                log.info("数据长度:{},数组长度{},内容{}", commonRespOrders.getTotal_count(), commonRespOrders.getOrders()
                    .size(), commonRespOrders.getOrders());
            });


    }

    @Test
    void getInvoices() {
        home24AccountRepository.findAll()
            .stream()
            .findFirst()
            .ifPresent(home24Account -> {
                Home24Client client = new Home24Client(home24Account);

                Home24InvoicesReq req = Home24InvoicesReq.builder()
                    .max(100)
                    .startDate("2024-12-01T14:30:00Z")
                    .endDate("2024-12-24T14:30:00Z")
                    .build();

                Home24CommonInvoicesResp<Object> invoices = client.getInvoices(req);
                log.info("data长度:{},数组长度{},内容{}", invoices.getTotal_count(), invoices.getInvoices()
                    .size(), invoices.getInvoices());
            });


    }


}