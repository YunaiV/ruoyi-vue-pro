package com.somle.bestbuy.service;

import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.test.core.ut.BaseDbUnitTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * @author: Wqh
 * @date: 2024/12/13 14:27
 */
@Slf4j
@Import({BestbuyService.class})
public class BestbuyServiceTest extends BaseDbUnitTest {
    @Resource
    BestbuyService service;

    @Test
    void test() {
        JSONObject orders = service.getOrders();
        log.info("{}",orders);
    }
}
