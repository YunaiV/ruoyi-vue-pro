package com.somle.bestbuy.service;

import cn.iocoder.yudao.framework.common.util.json.JSONObject;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
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
        JSONObject orders = service.getOrders();
        log.info("{}",orders);
    }
}
