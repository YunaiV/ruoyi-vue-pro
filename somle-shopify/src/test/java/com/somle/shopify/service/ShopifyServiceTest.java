package com.somle.shopify.service;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.shopify.repository.ShopifyTokenRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.HashMap;


@Disabled
@Slf4j
@Import({ShopifyClient.class})
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
}