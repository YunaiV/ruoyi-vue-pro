package com.somle.amazon.service;

import static org.junit.jupiter.api.Assertions.*;

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


    @Test
    void updateAccessToken() {
        amazonService.updateAccessToken();
    }
}