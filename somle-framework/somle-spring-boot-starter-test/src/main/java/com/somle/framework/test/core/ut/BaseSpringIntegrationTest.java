package com.somle.framework.test.core.ut;


import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest(classes = BaseSpringIntegrationTest.Application.class)
@ActiveProfiles("unit-test")
@EnableJpaRepositories(basePackages = { "${somle.base-package}.repository" })
@EntityScan("${somle.base-package}.model")
public class BaseSpringIntegrationTest {

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
//    @Import({
//        // DB 配置类
//        DataSourceAutoConfiguration.class, // Spring DB 自动配置类
//        DataSourceTransactionManagerAutoConfiguration.class, // Spring 事务自动配置类
////        HibernateJpaAutoConfiguration.class,
////        EccangService.class,
////        EccangTokenRepository.class,
//    })
//    public static class Application {
//    }

    //    @Configuration
//    @EnableAutoConfiguration

    //    @Import({
//    })
    @Transactional
//    @AutoConfigureCache
    @AutoConfigureDataJpa
//    @AutoConfigureTestDatabase
    @AutoConfigureTestEntityManager
//    @ImportAutoConfiguration
    @EnableIntegration
//    @EnableIntegrationManagement
    public static class Application {
    }

    @Resource
    private ApplicationContext applicationContext;

    @Test
    public void printAllBeans() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("Beans provided by Spring:");

        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
    }
}