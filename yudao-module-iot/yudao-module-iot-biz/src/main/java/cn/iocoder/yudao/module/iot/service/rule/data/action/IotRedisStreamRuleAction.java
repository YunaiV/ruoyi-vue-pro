package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkRedisStreamConfig;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
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
 * Redis Stream 的 {@link IotDataRuleAction} 实现类
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotRedisStreamRuleAction extends
        IotDataRuleCacheableAction<IotDataSinkRedisStreamConfig, RedisTemplate<String, Object>> {

    @Override
    public Integer getType() {
        return IotDataSinkTypeEnum.REDIS_STREAM.getType();
    }

    @Override
    public void execute(IotDeviceMessage message, IotDataSinkRedisStreamConfig config) throws Exception {
        // 1. 获取 RedisTemplate
        RedisTemplate<String, Object> redisTemplate = getProducer(config);

        // 2. 创建并发送 Stream 记录
        ObjectRecord<String, ?> record = StreamRecords.newRecord()
                .ofObject(JsonUtils.toJsonString(message)).withStreamKey(config.getTopic());
        String recordId = String.valueOf(redisTemplate.opsForStream().add(record));
        log.info("[execute][消息发送成功] messageId: {}, config: {}", recordId, config);
    }

    @Override
    protected RedisTemplate<String, Object> initProducer(IotDataSinkRedisStreamConfig config) {
        // 1.1 创建 Redisson 配置
        Config redissonConfig = new Config();
        SingleServerConfig serverConfig = redissonConfig.useSingleServer()
                .setAddress("redis://" + config.getHost() + ":" + config.getPort())
                .setDatabase(config.getDatabase());
        // 1.2 设置密码（如果有）
        if (StrUtil.isNotBlank(config.getPassword())) {
            serverConfig.setPassword(config.getPassword());
        }

        // 2.1 创建 RedisTemplate 并配置
        RedissonClient redisson = Redisson.create(redissonConfig);
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(new RedissonConnectionFactory(redisson));
        // 2.2 设置序列化器
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.json());
        template.setHashValueSerializer(RedisSerializer.json());
        template.afterPropertiesSet();
        return template;
    }

    @Override
    protected void closeProducer(RedisTemplate<String, Object> producer) throws Exception {
        RedisConnectionFactory factory = producer.getConnectionFactory();
        if (factory != null) {
            ((RedissonConnectionFactory) factory).destroy();
        }
    }

}
