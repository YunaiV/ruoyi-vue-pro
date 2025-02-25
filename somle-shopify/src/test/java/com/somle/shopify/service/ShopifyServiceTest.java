package com.somle.shopify.service;


import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.ut.BaseSpringTest;
import com.somle.shopify.model.ShopifyToken;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Slf4j
@Import({ShopifyService.class})
class ShopifyServiceTest extends BaseDbUnitTest {
    @Resource
    ShopifyService service;

    @Test
    void test() {
        log.info(service.client.getProducts().toString());
    }
}