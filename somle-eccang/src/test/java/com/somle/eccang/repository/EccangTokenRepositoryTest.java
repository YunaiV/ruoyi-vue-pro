package com.somle.eccang.repository;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class EccangTokenRepositoryTest extends SomleBaseDbUnitTest {

    @Resource
    EccangTokenRepository eccangTokenRepository;


    @Test
    public void testRepo() {
        Assertions.assertEquals(eccangTokenRepository.findAll().size(), 2);
        System.out.println("correct count");
    }

    public static class Application {
    }

}