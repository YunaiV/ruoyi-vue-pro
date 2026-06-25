package cn.iocoder.yudao.framework.redis.config;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.redisson.spring.starter.RedissonAutoConfigurationV4;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/**
 * Redis 配置类
 */
@AutoConfiguration(before = RedissonAutoConfigurationV4.class) // 目的：使用自己定义的 RedisTemplate Bean
public class YudaoRedisAutoConfiguration {

    /**
     * 创建 RedisTemplate Bean，使用 JSON 序列化方式
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 创建 RedisTemplate 对象
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置 RedisConnection 工厂。😈 它就是实现多种 Java Redis 客户端接入的秘密工厂。感兴趣的胖友，可以自己去撸下。
        template.setConnectionFactory(factory);
        // 使用 String 序列化方式，序列化 KEY 。
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        // 使用 JSON 序列化方式，序列化 VALUE
        RedisSerializer<?> redisSerializer = buildRedisSerializer();
        template.setValueSerializer(redisSerializer);
        template.setHashValueSerializer(redisSerializer);
        return template;
    }

    public static RedisSerializer<?> buildRedisSerializer() {
        // 基于 JsonUtils 的 ObjectMapper rebuild 出一个 Redis 专用实例，启用 DefaultTyping 写入 @class 类型信息。
        // 原因：Spring Data Redis 4.0 的 GenericJacksonJsonRedisSerializer 不再默认启用 DefaultTyping，
        //      会导致 @Cacheable 等场景反序列化时退化为 LinkedHashMap，无法还原为原始对象。
        ObjectMapper redisMapper = JsonUtils.getObjectMapper().rebuild()
                .activateDefaultTypingAsProperty(BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class).build(), DefaultTyping.NON_FINAL, "@class")
                .build();
        return new GenericJacksonJsonRedisSerializer(redisMapper);
    }

}
