package com.somle.autonomous.service;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.autonomous.repository.AutonomousAccountRepository;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

@Slf4j
@Import({AutonomousAccountRepository.class, AutonomousService.class})
class AutonomousServiceTest extends SomleBaseDbUnitTest {

    @Resource
    AutonomousAccountRepository autonomousAccountRepository;
    @Resource
    AutonomousService autonomousService;

    @Test
    @Transactional
    @Rollback(value = false)
    void flushAccessToken() {
        autonomousService.refreshOrObtainAccessToken();
    }
}