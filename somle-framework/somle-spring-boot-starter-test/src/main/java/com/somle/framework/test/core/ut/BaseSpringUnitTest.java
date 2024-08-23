package com.somle.framework.test.core.ut;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest(classes = BaseSpringUnitTest.Application.class)
@ActiveProfiles("unit-test")
//@EntityScan("${somle.base-package}.model")
public class BaseSpringUnitTest {

//    @BeforeAll
//    public static void init() {
//        System.out.println("初始化，准备测试信息");
//    }
//
//    @BeforeEach
//    public void start(){
//        System.out.println("开始测试...");
//    }
//
//    @AfterEach
//    public void end(){
//        System.out.println("测试完毕...");
//    }
//
//    @AfterAll
//    public static void close() {
//        System.out.println("结束，准备退出测试");
//    }

//    @BootstrapWith(SpringBootTestContextBootstrapper.class)
//    @ExtendWith(SpringExtension.class)
//    @OverrideAutoConfiguration(enabled = false)
//    @TypeExcludeFilters(DataJpaTypeExcludeFilter.class)
//    @Transactional
//    @AutoConfigureCache
//    @AutoConfigureDataJpa
//    @AutoConfigureTestDatabase
//    @AutoConfigureTestEntityManager
//    @ImportAutoConfiguration
//    @SpringBootConfiguration
//    @EnableAutoConfiguration


    //    @Configuration
//    @EnableAutoConfiguration


//    @AutoConfigureCache
//    @AutoConfigureTestDatabase
//    @ImportAutoConfiguration
    public static class Application {
    }
}