package cn.iocoder.dashboard.framework.redis.config;

import cn.hutool.core.net.NetUtil;
import cn.iocoder.dashboard.framework.redis.core.pubsub.AbstractChannelMessageListener;
import cn.iocoder.dashboard.framework.redis.core.stream.AbstractStreamMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.util.ErrorHandler;

import java.time.Duration;
import java.util.List;

/**
 * Redis 配置类
 */
@Configuration
@Slf4j
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 创建 RedisTemplate 对象
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置 RedisConnection 工厂。😈 它就是实现多种 Java Redis 客户端接入的秘密工厂。感兴趣的胖友，可以自己去撸下。
        template.setConnectionFactory(factory);
        // 使用 String 序列化方式，序列化 KEY 。
        template.setKeySerializer(RedisSerializer.string());
        // 使用 JSON 序列化方式（库是 Jackson ），序列化 VALUE 。
        template.setValueSerializer(RedisSerializer.json());
        return template;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory factory,
                                                                       List<AbstractChannelMessageListener<?>> listeners) {
        // 创建 RedisMessageListenerContainer 对象
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        // 设置 RedisConnection 工厂。
        container.setConnectionFactory(factory);
        // 添加监听器
        listeners.forEach(listener -> {
            container.addMessageListener(listener, new ChannelTopic(listener.getChannel()));
            log.info("[redisMessageListenerContainer][注册 Channel({}) 对应的监听器({})]",
                    listener.getChannel(), listener.getClass().getName());
        });
        return container;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> redisStreamMessageListenerContainer(
            RedisConnectionFactory factory, List<AbstractStreamMessageListener<?>> listeners) {
        // 创建配置对象
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>>
                streamMessageListenerContainerOptions = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                    .builder()
                    // 一次性最多拉取多少条消息
                    .batchSize(10)
                    // 执行消息轮询的执行器
                    // .executor(this.threadPoolTaskExecutor)
                    // 消息消费异常的handler
                    .errorHandler(new ErrorHandler() {
                        @Override
                        public void handleError(Throwable t) {
                            // throw new RuntimeException(t);
                            t.printStackTrace();
                        }
                    })
                    // 超时时间，设置为0，表示不超时（超时后会抛出异常）
                    .pollTimeout(Duration.ZERO)
                    // 序列化器
                    .serializer(RedisSerializer.string())
                    .targetType(String.class)
                    .build();

        // 根据配置对象创建监听容器对象
        StreamMessageListenerContainer<String, ObjectRecord<String, String>> container = StreamMessageListenerContainer
                .create(factory, streamMessageListenerContainerOptions);

        RedisTemplate<String, Object> redisTemplate = redisTemplate(factory);

        // 使用监听容器对象开始监听消费（使用的是手动确认方式）
        String consumerName = NetUtil.getLocalHostName(); // TODO 需要优化下，晚点参考下 rocketmq consumer 的
        for (AbstractStreamMessageListener<?> listener : listeners) {
            try {
                redisTemplate.opsForStream().createGroup(listener.getStreamKey(), listener.getGroup());
            } catch (Exception ignore) {
//                ignore.printStackTrace();
            }

            container.receive(Consumer.from(listener.getGroup(), consumerName),
                    StreamOffset.create(listener.getStreamKey(), ReadOffset.lastConsumed()), listener);
        }

        return container;
    }

}
