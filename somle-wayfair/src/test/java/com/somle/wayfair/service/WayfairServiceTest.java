package com.somle.wayfair.service;


import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

@Disabled
@Slf4j
@Import({WayfairService.class})
class WayfairServiceTest extends SomleBaseDbUnitTest {
    @Resource
    WayfairService service;

    @Test
    void test() {
        for (WayfairClient client : service.clients) {
            client.refreshAccessToken();
        }
    }

    @Test
    void test1() {
        for (WayfairClient client : service.clients) {
            var result = client.getOrders(LocalDateTime.of(2025, 05, 12, 0, 0, 0));
            log.info(result.toString());
        }
    }
}