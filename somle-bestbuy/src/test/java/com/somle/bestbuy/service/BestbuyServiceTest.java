package com.somle.bestbuy.service;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * @author: Wqh
 * @date: 2024/12/13 14:27
 */
@Disabled
@Slf4j
@Import({BestbuyService.class})
public class BestbuyServiceTest extends SomleBaseDbUnitTest {
    @Resource
    BestbuyService service;

    @Test
    void test() {
        service.bestbuyClients.forEach(bestbuyClient -> {
            bestbuyClient.getAllOrders();
            log.info("{}", 11);
        });
    }
}
