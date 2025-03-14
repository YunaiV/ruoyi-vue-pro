package cn.iocoder.yudao.framework.test.core.ut;


import org.junit.jupiter.api.Disabled;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = SomleBaseSpringTest.Application.class)
@ActiveProfiles("unit-test")
@EnableJpaRepositories(basePackages = { "${somle.base-package}.repository" })
@EntityScan("${somle.base-package}.model")
public class SomleBaseSpringTest {

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
    public static class Application {
    }
}