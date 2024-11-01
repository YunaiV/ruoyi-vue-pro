package com.somle.wayfair.service;


import com.somle.framework.test.core.ut.BaseDbUnitTest;
import com.somle.wayfair.service.WayfairService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Slf4j
@Import({WayfairService.class})
class WayfairServiceTest extends BaseDbUnitTest {
    @Resource
    WayfairService service;

    @Test
    void test() {
        service.client.refreshAccessToken();
    }

    @Test
    void test1() {
        var result = service.client.getOrders();
        log.info(result);
    }
}