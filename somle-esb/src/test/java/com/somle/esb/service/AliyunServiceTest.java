package com.somle.esb.service;

import cn.iocoder.yudao.framework.test.core.ut.BaseSpringTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@Import(AliyunService.class)
class AliyunServiceTest extends BaseSpringTest {
    @Test
    public void retrieveToken() {
    }

}