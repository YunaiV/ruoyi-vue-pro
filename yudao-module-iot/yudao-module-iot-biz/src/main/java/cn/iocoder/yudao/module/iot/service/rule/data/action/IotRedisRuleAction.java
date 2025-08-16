package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkRedisConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotRedisDataStructureEnum;
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

import java.util.Map;

/**
 * Redis 的 {@link IotDataRuleAction} 实现类
 * 支持多种 Redis 数据结构：Stream、Hash、List、Set、ZSet、String
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotRedisRuleAction extends
        IotDataRuleCacheableAction<IotDataSinkRedisConfig, RedisTemplate<String, Object>> {

    @Override
    public Integer getType() {
        return IotDataSinkTypeEnum.REDIS.getType();
    }

    @Override
    public void execute(IotDeviceMessage message, IotDataSinkRedisConfig config) throws Exception {
        // 1. 获取 RedisTemplate
        RedisTemplate<String, Object> redisTemplate = getProducer(config);

        // 2. 根据数据结构类型执行不同的操作
        String messageJson = JsonUtils.toJsonString(message);
        IotRedisDataStructureEnum dataStructure = getDataStructureByType(config.getDataStructure());
        switch (dataStructure) {
            case STREAM:
                executeStream(redisTemplate, config, messageJson);
                break;
            case HASH:
                executeHash(redisTemplate, config, message, messageJson);
                break;
            case LIST:
                executeList(redisTemplate, config, messageJson);
                break;
            case SET:
                executeSet(redisTemplate, config, messageJson);
                break;
            case ZSET:
                executeZSet(redisTemplate, config, message, messageJson);
                break;
            case STRING:
                executeString(redisTemplate, config, messageJson);
                break;
            default:
                throw new IllegalArgumentException("不支持的 Redis 数据结构类型: " + dataStructure);
        }

        log.info("[execute][消息发送成功] dataStructure: {}, config: {}", dataStructure.getName(), config);
    }

    /**
     * 执行 Stream 操作
     */
    private void executeStream(RedisTemplate<String, Object> redisTemplate, IotDataSinkRedisConfig config, String messageJson) {
        ObjectRecord<String, ?> record = StreamRecords.newRecord()
                .ofObject(messageJson).withStreamKey(config.getTopic());
        redisTemplate.opsForStream().add(record);
    }

    /**
     * 执行 Hash 操作
     */
    private void executeHash(RedisTemplate<String, Object> redisTemplate, IotDataSinkRedisConfig config,
                             IotDeviceMessage message, String messageJson) {
        String hashField = StrUtil.isNotBlank(config.getHashField()) ?
                config.getHashField() : String.valueOf(message.getDeviceId());
        redisTemplate.opsForHash().put(config.getTopic(), hashField, messageJson);
    }

    /**
     * 执行 List 操作
     */
    private void executeList(RedisTemplate<String, Object> redisTemplate, IotDataSinkRedisConfig config, String messageJson) {
        redisTemplate.opsForList().rightPush(config.getTopic(), messageJson);
    }

    /**
     * 执行 Set 操作
     */
    private void executeSet(RedisTemplate<String, Object> redisTemplate, IotDataSinkRedisConfig config, String messageJson) {
        redisTemplate.opsForSet().add(config.getTopic(), messageJson);
    }

    /**
     * 执行 ZSet 操作
     */
    private void executeZSet(RedisTemplate<String, Object> redisTemplate, IotDataSinkRedisConfig config,
                             IotDeviceMessage message, String messageJson) {
        double score;
        if (StrUtil.isNotBlank(config.getScoreField())) {
            // 尝试从消息中获取分数字段
            try {
                Map<String, Object> messageMap = JsonUtils.parseObject(messageJson, Map.class);
                Object scoreValue = messageMap.get(config.getScoreField());
                score = scoreValue instanceof Number ? ((Number) scoreValue).doubleValue() : System.currentTimeMillis();
            } catch (Exception e) {
                score = System.currentTimeMillis();
            }
        } else {
            // 使用当前时间戳作为分数
            score = System.currentTimeMillis();
        }
        redisTemplate.opsForZSet().add(config.getTopic(), messageJson, score);
    }

    /**
     * 执行 String 操作
     */
    private void executeString(RedisTemplate<String, Object> redisTemplate, IotDataSinkRedisConfig config, String messageJson) {
        redisTemplate.opsForValue().set(config.getTopic(), messageJson);
    }

    @Override
    protected RedisTemplate<String, Object> initProducer(IotDataSinkRedisConfig config) {
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

    /**
     * 根据类型值获取数据结构枚举
     */
    private IotRedisDataStructureEnum getDataStructureByType(Integer type) {
        for (IotRedisDataStructureEnum dataStructure : IotRedisDataStructureEnum.values()) {
            if (dataStructure.getType().equals(type)) {
                return dataStructure;
            }
        }
        throw new IllegalArgumentException("不支持的 Redis 数据结构类型: " + type);
    }

}
