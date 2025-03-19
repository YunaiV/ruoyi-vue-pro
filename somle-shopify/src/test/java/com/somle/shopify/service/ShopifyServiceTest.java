package com.somle.shopify.service;


import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Disabled
@Slf4j
@Import({ShopifyService.class})
class ShopifyServiceTest extends SomleBaseDbUnitTest {
    @Resource
    ShopifyService service;

    @Test
    void test() {
        log.info(service.client.getProducts().toString());
    }
}