package com.somle.autonomous.service;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

@Disabled
@Slf4j
@Import({AutonomousService.class})
class AutonomousServiceTest extends SomleBaseDbUnitTest {

    @Resource
    AutonomousService autonomousService;

    @Test
    @Transactional
    @Rollback(value = false)
    void flushAccessToken() {
        autonomousService.refreshOrObtainAccessToken();
    }

    @Test
    void test() {
        autonomousService.autonomousClients.get(0).getAllProduct();
    }
}