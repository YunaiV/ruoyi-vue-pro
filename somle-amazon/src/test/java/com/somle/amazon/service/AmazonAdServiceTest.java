package com.somle.amazon.service;

import com.somle.framework.common.util.general.CoreUtils;
import com.somle.framework.test.core.ut.BaseSpringTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;


@Slf4j
@Import({
    AmazonService.class,
    AmazonAdService.class,
})
class AmazonAdServiceTest extends BaseSpringTest {
    @Resource
    AmazonAdService service;


    @Test
    void refreshAuth() {
        service.refreshAuth();
    }


}