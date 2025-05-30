package com.somle.home24.service;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.home24.model.pojo.Home24Account;
import com.somle.home24.model.req.Home24InvoicesReq;
import com.somle.home24.model.req.Home24OrderReq;
import com.somle.home24.model.resp.Home24CommonInvoicesResp;
import com.somle.home24.repository.Home24AccountRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
//    @Rollback(value = false)
    void getOrder() {
        System.out.println("开始");
        List<Home24Account> accountList = home24AccountRepository.findAll();

        home24AccountRepository.findAll()
            .stream()
            .findFirst()
            .ifPresent(home24Account -> {
                Home24Client client = new Home24Client(home24Account);


                String param = "2025-05-26";
                LocalDate baseDate = LocalDate.parse(param);
                // 2. 计算前两天的日期
                LocalDate targetDate = baseDate.minusDays(2);
                // 3. 生成起始时间（00:00:00）
                LocalDateTime startTime = targetDate.atStartOfDay();
                // 4. 生成结束时间（23:59:59）
                LocalDateTime endTime = targetDate.atTime(23, 59, 59);

                Home24OrderReq req = Home24OrderReq.builder()
                    .max(100)
                    .startDate(startTime.toString())
                    .endDate(endTime.toString())
                    .build();

                client.getAllOrders(req);

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


    @Test
    void getShopInformation() {
        home24AccountRepository.findAll()
            .stream()
            .findFirst()
            .ifPresent(home24Account -> {
                Home24Client client = new Home24Client(home24Account);
                client.getShopInformation();
            });
    }


}