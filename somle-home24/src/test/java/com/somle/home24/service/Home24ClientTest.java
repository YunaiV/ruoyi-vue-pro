package com.somle.home24.service;

import com.somle.framework.test.core.ut.BaseDbUnitTest;
import com.somle.home24.model.req.Home24InvoicesReq;
import com.somle.home24.model.req.Home24OrderReq;
import com.somle.home24.model.resp.Home24CommonRespInvoices;
import com.somle.home24.model.resp.Home24CommonRespOrders;
import com.somle.home24.repository.Home24AccountDao;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

@Slf4j
@Import(Home24AccountDao.class)
class Home24ClientTest extends BaseDbUnitTest {
    @Resource
    Home24AccountDao home24AccountDao;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @Rollback(value = false)
    void getOrder() {
        home24AccountDao.findAll()
                .stream()
                .findFirst()
                .ifPresent(home24Account -> {
                    Home24Client client = new Home24Client(home24Account);

                    Home24OrderReq req = Home24OrderReq.builder()
                            .max(100)
                            .startDate("2024-12-01T14:30:00Z")
                            .endDate("2024-12-24T14:30:00Z")
                            .build();
                    Home24CommonRespOrders<Object> commonRespOrders = client.getOrder(req);
                    log.info("数据长度:{},数组长度{{},内容{}", commonRespOrders.getTotal_count()
                            , commonRespOrders.getOrders()
                                    .size()
                            , commonRespOrders.getOrders());
                });


    }

    @Test
    void getInvoices() {
        home24AccountDao.findAll()
                .stream()
                .findFirst()
                .ifPresent(home24Account -> {
                    Home24Client client = new Home24Client(home24Account);

                    Home24InvoicesReq req = Home24InvoicesReq.builder()
                            .max(100)
                            .startDate("2024-12-01T14:30:00Z")
                            .endDate("2024-12-24T14:30:00Z")
                            .build();
                    Home24CommonRespInvoices<Object> invoices = client.getInvoices(req);
                    log.info("data长度:{},数组长度{{},内容{}", invoices.getTotal_count()
                            , invoices.getInvoices()
                                    .size()
                            , invoices.getInvoices());
                });


    }


}