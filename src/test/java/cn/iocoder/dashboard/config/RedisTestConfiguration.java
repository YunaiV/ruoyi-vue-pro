package cn.iocoder.dashboard.config;

import com.github.fppt.jedismock.RedisServer;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RedisProperties.class)
@AutoConfigureBefore({RedisAutoConfiguration.class, RedissonAutoConfiguration.class}) // 在 Redis 自动配置前，进行初始化
public class RedisTestConfiguration {

    /**
     * 创建模拟的 Redis Server 服务器
     */
    @Bean(destroyMethod = "stop")
    public RedisServer redisServer(RedisProperties properties) throws IOException {
        RedisServer redisServer = new RedisServer(properties.getPort());
        // TODO 芋艿：一次执行多个单元测试时，貌似创建多个 spring 容器，导致不进行 stop。这样，就导致端口被占用，无法启动。。。
        try {
            redisServer.start();
        } catch (Exception ignore) {}
        return redisServer;
    }

}
