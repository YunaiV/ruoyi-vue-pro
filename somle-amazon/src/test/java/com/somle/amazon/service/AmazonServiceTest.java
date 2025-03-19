package com.somle.amazon.service;

import cn.iocoder.yudao.framework.common.util.general.CoreUtils;
import cn.iocoder.yudao.framework.test.core.ut.SomleBaseSpringTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.Import;


@Disabled
@Slf4j
@Import({
    AmazonService.class,
})
class AmazonServiceTest extends SomleBaseSpringTest {
    @Resource
    AmazonService amazonService;



    static boolean run = true;


//    @Test
//    void refreshAuth() {
//        amazonService.refreshAuth();
//    }



    @Test
    void visibility() throws InterruptedException {
        Thread t = new Thread(()->{
            while (run) {
            }
        });
        t.start();
        CoreUtils.sleep(1000);
        log.info("stop");
        run = false;
        t.join();
    }

}