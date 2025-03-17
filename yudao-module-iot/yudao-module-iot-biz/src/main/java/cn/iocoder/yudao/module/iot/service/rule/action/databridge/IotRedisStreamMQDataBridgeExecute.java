package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config.IotDataBridgeRedisStreamMQConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgeTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

/**
 * Redis Stream MQ 的 {@link IotDataBridgeExecute} 实现类
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotRedisStreamMQDataBridgeExecute extends
        AbstractCacheableDataBridgeExecute<IotDataBridgeRedisStreamMQConfig, RedisTemplate<String, Object>> {

    @Override
    public Integer getType() {
        return IotDataBridgeTypeEnum.REDIS_STREAM.getType();
    }

    @Override
    public void execute0(IotDeviceMessage message, IotDataBridgeRedisStreamMQConfig config) throws Exception {
        // 1. 获取 RedisTemplate
        RedisTemplate<String, Object> redisTemplate = getProducer(config);

        // 2. 创建并发送 Stream 记录
        ObjectRecord<String, IotDeviceMessage> record = StreamRecords.newRecord()
                .ofObject(message).withStreamKey(config.getTopic());
        String recordId = String.valueOf(redisTemplate.opsForStream().add(record));
        log.info("[executeRedisStream][消息发送成功] messageId: {}, config: {}", recordId, config);
    }

    @Override
    protected RedisTemplate<String, Object> initProducer(IotDataBridgeRedisStreamMQConfig config) {
        // 1.1 创建 Redisson 配置
        Config redissonConfig = new Config();
        SingleServerConfig serverConfig = redissonConfig.useSingleServer()
                .setAddress("redis://" + config.getHost() + ":" + config.getPort())
                .setDatabase(config.getDatabase());
        // 1.2 设置密码（如果有）
        if (StrUtil.isNotBlank(config.getPassword())) {
            serverConfig.setPassword(config.getPassword());
        }

        // TODO @huihui：看看能不能简化一些。按道理说，不用这么多的哈。
        // 2.1 创建 RedissonClient
        RedissonClient redisson = Redisson.create(redissonConfig);
        // 2.2 创建并配置 RedisTemplate
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置 RedisConnection 工厂。😈 它就是实现多种 Java Redis 客户端接入的秘密工厂。感兴趣的胖友，可以自己去撸下。
        template.setConnectionFactory(new RedissonConnectionFactory(redisson));
        // 使用 String 序列化方式，序列化 KEY 。
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        // 使用 JSON 序列化方式（库是 Jackson ），序列化 VALUE 。
        template.setValueSerializer(buildRedisSerializer());
        template.setHashValueSerializer(buildRedisSerializer());
        template.afterPropertiesSet();// 初始化
        return template;
    }

    @Override
    protected void closeProducer(RedisTemplate<String, Object> producer) throws Exception {
        RedisConnectionFactory factory = producer.getConnectionFactory();
        if (factory != null) {
            ((RedissonConnectionFactory) factory).destroy();
        }
    }

    // TODO @huihui：看看能不能简化一些。按道理说，不用这么多的哈。
    public static RedisSerializer<?> buildRedisSerializer() {
        RedisSerializer<Object> json = RedisSerializer.json();
        // 解决 LocalDateTime 的序列化
        ObjectMapper objectMapper = (ObjectMapper) ReflectUtil.getFieldValue(json, "mapper");
        objectMapper.registerModules(new JavaTimeModule());
        return json;
    }

}
