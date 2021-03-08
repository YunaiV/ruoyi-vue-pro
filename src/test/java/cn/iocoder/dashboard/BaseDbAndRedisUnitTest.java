package cn.iocoder.dashboard;

import cn.iocoder.dashboard.config.RedisTestConfiguration;
import cn.iocoder.dashboard.framework.datasource.config.DataSourceConfiguration;
import cn.iocoder.dashboard.framework.mybatis.config.MybatisConfiguration;
import cn.iocoder.dashboard.framework.redis.config.RedisConfig;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

/**
 * 依赖内存 DB 的单元测试
 *
 * 注意，Service 层同样适用。对于 Service 层的单元测试，我们针对自己模块的 Mapper 走的是 H2 内存数据库，针对别的模块的 Service 走的是 Mock 方法
 *
 * @author 芋道源码
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = BaseDbAndRedisUnitTest.Application.class)
@ActiveProfiles("unit-test") // 设置使用 application-unit-test 配置文件
@Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD) // 每个单元测试结束后，清理 DB
public class BaseDbAndRedisUnitTest {

    @Import({
            // DB 配置类
            DataSourceConfiguration.class, // 自己的 DB 配置类
            DataSourceAutoConfiguration.class, // Spring DB 自动配置类
            DruidDataSourceAutoConfigure.class, // Druid 自动配置类
            // MyBatis 配置类
            MybatisConfiguration.class, // 自己的 MyBatis 配置类
            MybatisPlusAutoConfiguration.class, // MyBatis 的自动配置类
            // Redis 配置类
            RedisTestConfiguration.class, // Redis 测试配置类，用于启动 RedisServer
            RedisAutoConfiguration.class, // Spring Redis 自动配置类
            RedisConfig.class, // 自己的 Redis 配置类
            RedissonAutoConfiguration.class, // Redisson 自动高配置类
    })
    public static class Application {
    }

}
