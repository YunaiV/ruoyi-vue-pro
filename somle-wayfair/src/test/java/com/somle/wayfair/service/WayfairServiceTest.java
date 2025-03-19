package com.somle.wayfair.service;


import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.wayfair.service.WayfairService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Disabled
@Slf4j
@Import({WayfairService.class})
class WayfairServiceTest extends SomleBaseDbUnitTest {
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