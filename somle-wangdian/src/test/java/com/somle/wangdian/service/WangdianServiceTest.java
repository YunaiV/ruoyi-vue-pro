package com.somle.wangdian.service;

import com.somle.framework.test.core.ut.BaseSpringTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;

@Slf4j
@Import(WangdianService.class)
class WangdianServiceTest extends BaseSpringTest {

    @Resource
    WangdianService service;






}