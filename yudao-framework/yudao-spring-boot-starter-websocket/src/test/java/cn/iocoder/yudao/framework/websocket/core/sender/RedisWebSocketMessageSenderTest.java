package cn.iocoder.yudao.framework.websocket.core.sender;

import cn.iocoder.yudao.framework.mq.redis.config.YudaoRedisMQConsumerAutoConfiguration;
import cn.iocoder.yudao.framework.mq.redis.config.YudaoRedisMQProducerAutoConfiguration;
import cn.iocoder.yudao.framework.mq.redis.core.RedisMQTemplate;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.redis.config.YudaoRedisAutoConfiguration;
import cn.iocoder.yudao.framework.test.config.RedisTestConfiguration;
import org.redisson.spring.starter.RedissonAutoConfigurationV4;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import cn.iocoder.yudao.framework.websocket.core.sender.redis.RedisWebSocketMessageConsumer;
import cn.iocoder.yudao.framework.websocket.core.sender.redis.RedisWebSocketMessageSender;
import cn.iocoder.yudao.framework.websocket.core.session.WebSocketSessionManagerImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

/**
 * {@link RedisWebSocketMessageSender} 的单元测试
 *
 * @author 芋道源码
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = RedisWebSocketMessageSenderTest.Application.class)
@ActiveProfiles("unit-test")
public class RedisWebSocketMessageSenderTest {

    /**
     * 与 BaseRedisUnitTest 相同的内存 Redis 配置，额外引入 Redis MQ 的生产者、消费者配置
     */
    @Import({
            RedisTestConfiguration.class, // Redis 测试配置类，用于启动 RedisServer
            YudaoRedisAutoConfiguration.class, // 自己的 Redis 配置类，需要先于 DataRedisAutoConfiguration 注册 redisTemplate
            SpringUtil.class,
            SenderConfig.class, // 先注册 Consumer，YudaoRedisMQConsumerAutoConfiguration 才会创建 Pub/Sub 监听容器
            YudaoRedisMQProducerAutoConfiguration.class, YudaoRedisMQConsumerAutoConfiguration.class,
            // Spring Boot 4 的 DataRedisAutoConfiguration 也会按名称注册 redisMessageListenerContainer，需放在 Redis MQ 之后，由其 @ConditionalOnMissingBean 跳过
            DataRedisAutoConfiguration.class, // Spring Redis 自动配置类
            RedissonAutoConfigurationV4.class // Redisson 自动配置类
    })
    public static class Application {
    }

    @Configuration(proxyBeanMethods = false)
    public static class SenderConfig {

        @Bean
        public RedisWebSocketMessageSender redisWebSocketMessageSender(RedisMQTemplate redisMQTemplate) {
            WebSocketSessionManagerImpl sessionManager = new WebSocketSessionManagerImpl();
            sessionManager.addSession(SESSION);
            return new RedisWebSocketMessageSender(sessionManager, redisMQTemplate);
        }

        @Bean
        public RedisWebSocketMessageConsumer redisWebSocketMessageConsumer(RedisWebSocketMessageSender sender) {
            return new RedisWebSocketMessageConsumer(sender);
        }

    }

    private static final WebSocketSession SESSION = LocalWebSocketMessageSenderTest.mockSession("ws-1", 1L, 1, 100L);

    @Resource
    private RedisWebSocketMessageSender sender;

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void testSendAndConsume() {
        // 准备参数
        TenantContextHolder.setTenantId(1L);

        // 调用
        sender.send(1, 100L, "notify-message-receive", "{\"id\":1}");
        // 断言：经 Redis Pub/Sub 广播后，投递到本节点的 WebSocket 连接
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> verify(SESSION).sendMessage(
                argThat(message -> ((TextMessage) message).getPayload().contains("notify-message-receive"))));
    }

}
