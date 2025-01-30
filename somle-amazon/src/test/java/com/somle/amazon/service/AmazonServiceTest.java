package com.somle.amazon.service;

import com.somle.framework.common.util.general.CoreUtils;
import com.somle.framework.test.core.ut.BaseSpringTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.Import;


@Slf4j
@Import({
    AmazonService.class,
})
class AmazonServiceTest extends BaseSpringTest {
    @Resource
    AmazonService amazonService;



    static boolean run = true;


//    @Test
//    void refreshAuth() {
//        amazonService.refreshAuth();
//    }

//    @Test
//    void generateAccessToken() {
//        var clientId = amazonService.accounts.get(0).getAdClientId();
//        var clientSecret = amazonService.accounts.get(0).getAdClientSecret();
//        var code = "";
//        var response = amazonService.generateAccessToken(clientId, clientSecret, code);
//        log.info(response);
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