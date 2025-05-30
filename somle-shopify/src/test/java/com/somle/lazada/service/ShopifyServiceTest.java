package com.somle.lazada.service;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.shopify.repository.ShopifyTokenRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;


@Disabled
@Slf4j
class ShopifyServiceTest extends SomleBaseDbUnitTest {
//    @Resource
//    ShopifyClient client;

    @Resource
    ShopifyTokenRepository shopifyTokenRepository;

//    @Test
//    void test() {
//        log.info(client.getProducts(new HashMap<>()).toString());
//    }

    @Test
    void test() {
        ShopifyClient client = new ShopifyClient(shopifyTokenRepository.findAll().get(0));
        log.info(client.getProducts(new HashMap<>()).toString());
    }

    @Test
    void test2() {
        ShopifyClient client = new ShopifyClient(shopifyTokenRepository.findAll().get(0));
        log.info(client.getPayouts().toString());
    }

    @Test
    void test3() {
        ShopifyClient client = new ShopifyClient(shopifyTokenRepository.findAll().get(0));
        String param = "2025-05-14";
        LocalDate baseDate = LocalDate.parse(param);
        // 2. 计算前两天的日期
        LocalDate targetDate = baseDate.minusDays(2);
        // 3. 生成起始时间（00:00:00）
        LocalDateTime startTime = targetDate.atStartOfDay();
        // 4. 生成结束时间（23:59:59）
        LocalDateTime endTime = targetDate.atTime(23, 59, 59);
        log.info(client.getAllOrders(startTime, endTime).toString());
    }

    @Test
    void test4() {
        ShopifyClient client = new ShopifyClient(shopifyTokenRepository.findAll().get(0));
        log.info(client.getShop().toString());
    }
}