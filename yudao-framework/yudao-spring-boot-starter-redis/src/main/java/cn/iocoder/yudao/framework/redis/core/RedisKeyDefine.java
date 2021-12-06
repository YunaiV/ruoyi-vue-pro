package cn.iocoder.yudao.framework.redis.core;

import com.fasterxml.jackson.annotation.JsonValue;
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

    @Getter
    @AllArgsConstructor
    public enum KeyTypeEnum {

        STRING("String"),
        LIST("List"),
        HASH("Hash"),
        SET("Set"),
        ZSET("Sorted Set"),
        STREAM("Stream"),
        PUBSUB("Pub/Sub");

        /**
         * 类型
         */
        @JsonValue
        private final String type;

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
        @JsonValue
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
    /**
     * 备注
     */
    private final String memo;

    private RedisKeyDefine(String memo, String keyTemplate, KeyTypeEnum keyType, Class<?> valueType,
                           TimeoutTypeEnum timeoutType, Duration timeout) {
        this.memo = memo;
        this.keyTemplate = keyTemplate;
        this.keyType = keyType;
        this.valueType = valueType;
        this.timeout = timeout;
        this.timeoutType = timeoutType;
        // 添加注册表
        RedisKeyRegistry.add(this);
    }

    public RedisKeyDefine(String memo, String keyTemplate, KeyTypeEnum keyType, Class<?> valueType, Duration timeout) {
        this(memo, keyTemplate, keyType, valueType, TimeoutTypeEnum.FIXED, timeout);
    }

    public RedisKeyDefine(String memo, String keyTemplate, KeyTypeEnum keyType, Class<?> valueType, TimeoutTypeEnum timeoutType) {
        this(memo, keyTemplate, keyType, valueType, timeoutType, Duration.ZERO);
    }

    /**
     * 格式化 Key
     *
     * 注意，内部采用 {@link String#format(String, Object...)} 实现
     *
     * @param args 格式化的参数
     * @return Key
     */
    public String formatKey(Object... args) {
        return String.format(keyTemplate, args);
    }

}
