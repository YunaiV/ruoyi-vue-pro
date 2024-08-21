package com.somle.eccang.repository;

import com.somle.framework.test.core.ut.BaseDbUnitTest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class EccangTokenRepositoryTest extends BaseDbUnitTest {

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