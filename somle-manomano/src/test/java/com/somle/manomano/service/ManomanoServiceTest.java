package com.somle.manomano.service;


import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Disabled
@Slf4j
@Import({ManomanoService.class})
class ManomanoServiceTest extends SomleBaseDbUnitTest {
    @Resource
    ManomanoService service;

    @Test
    void getOrders() {
        String param = "2025-05-24";
        LocalDate baseDate = LocalDate.parse(param);
        // 2. 计算前两天的日期
        LocalDate targetDate = baseDate.minusDays(2);


        String s = targetDate.format(DateTimeFormatter.ISO_DATE);

        String startTime = s + "T00:00:00Z";

        String endTime = s + "T23:59:59Z";

        log.info(service.clients.get(0).getAllOrders(startTime, endTime).toString());
    }

    @Test
    void getOffersInfo() {
        log.info(service.clients.get(0).getAllProducts().toString());
    }

}