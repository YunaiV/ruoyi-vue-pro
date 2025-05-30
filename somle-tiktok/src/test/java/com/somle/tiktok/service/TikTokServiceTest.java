package com.somle.tiktok.service;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;


@Disabled
@Slf4j
@Import({TikTokService.class})
class TikTokServiceTest extends SomleBaseDbUnitTest {
    @Resource
    TikTokService tikTokService;


    @Test
    @Rollback(false)
    void refreshToken() throws Exception {
        tikTokService.refreshAccessToken();
    }

    @Test
    void test() {

        tikTokService.tikTokClients.forEach(tikTokClient -> {
            tikTokClient.getAllProducts();
        });
    }

    @Test
    void test1() throws Exception {
        tikTokService.tikTokClients.forEach(tikTokClient -> {
            try {
                tikTokClient.getShop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void test2() throws Exception {
        tikTokService.tikTokClients.forEach(tikTokClient -> {
            try {
                tikTokClient.getOrder();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}