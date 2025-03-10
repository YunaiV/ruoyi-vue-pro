package com.somle.cdiscount.service;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Slf4j
@Import({CdiscountService.class})
class CdiscountServiceTest extends SomleBaseDbUnitTest {
    @Resource
    CdiscountService service;

    @Test
    void test() {
        log.info(service.client.getOrders().toString());
    }
}