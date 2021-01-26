package cn.iocoder.dashboard.framework.redis.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.Duration;

/**
 * Redis Key 定义类
 *
 * @author 芋道源码
 */
@Data
public class RedisKeyDefine {

    public enum KeyTypeEnum {

        STRING,
        LIST,
        HASH,
        SET,
        ZSET,
        STREAM,
        PUBSUB

    }

    @Getter
    @AllArgsConstructor
    public enum TimeoutTypeEnum {

        FOREVER(1), // 永不超时
        DYNAMIC(2), // 动态超时
        FIXED(3); // 固定超时

        /**
         * 类型
         */
        private final Integer type;

    }

    /**
     * Key 模板
     */
    private final String keyTemplate;
    /**
     * Key 类型的枚举
     */
    private final KeyTypeEnum keyType;
    /**
     * Value 类型
     *
     * 如果是使用分布式锁，设置为 {@link java.util.concurrent.locks.Lock} 类型
     */
    private final Class<?> valueType;
    /**
     * 超时类型
     */
    private final TimeoutTypeEnum timeoutType;
    /**
     * 过期时间
     */
    private final Duration timeout;

    public RedisKeyDefine(String keyTemplate, KeyTypeEnum keyType, Class<?> valueType, Duration timeout) {
        this.keyTemplate = keyTemplate;
        this.keyType = keyType;
        this.valueType = valueType;
        this.timeoutType = TimeoutTypeEnum.FIXED;
        this.timeout = timeout;
        // 添加注册表
        RedisKeyRegistry.add(this);
    }

    public RedisKeyDefine(String keyTemplate, KeyTypeEnum keyType, Class<?> valueType, TimeoutTypeEnum timeoutType) {
        this.keyTemplate = keyTemplate;
        this.keyType = keyType;
        this.valueType = valueType;
        this.timeoutType = timeoutType;
        this.timeout = Duration.ZERO;
        // 添加注册表
        RedisKeyRegistry.add(this);
    }

}
