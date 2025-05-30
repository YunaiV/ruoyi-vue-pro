package com.somle.cdiscount.service;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Disabled
@Slf4j
@Import({CdiscountService.class})
class CdiscountServiceTest extends SomleBaseDbUnitTest {
    @Resource
    CdiscountService service;

    @Test
    void test() {
        LocalDate baseDate = LocalDate.parse("2025-05-25");
        // 2. 计算前两天的日期
        LocalDate targetDate = baseDate.minusDays(2);
        // 3. 生成起始时间（00:00:00）
        LocalDateTime startTime = targetDate.atStartOfDay();
        // 4. 生成结束时间（23:59:59）
        LocalDateTime endTime = targetDate.atTime(23, 59, 59);
        log.info(service.clients.get(0).getAllOrders(startTime, endTime).toString());
    }

    @Test
    void test2() {
        log.info(service.clients.get(0).getSeller().toString());
    }

    @Test
    void test3() {
        log.info(service.clients.get(0).getAllProducts().toString());
    }

    @Test
    @Rollback(value = false)
    void test4() {
        service.refreshAuths();
    }
}