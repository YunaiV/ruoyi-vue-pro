package com.somle.manomano.service;


import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

@Disabled
@Slf4j
@Import({ManomanoService.class})
class ManomanoServiceTest extends SomleBaseDbUnitTest {
    @Resource
    ManomanoService service;

    @Test
    void getOrders() {
        var start = LocalDateTime.now().minusDays(1);
        var end = LocalDateTime.now();


        log.info(service.getClient().getOrders().toString());
    }

}