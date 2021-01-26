package cn.iocoder.dashboard.modules.infra.controller.redis.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class InfRedisKeyRespVO {

    /**
     * Key 模板
     */
    private final String keyTemplate;
    /**
     * Key 类型的枚举
     */
    private final String keyType;
    /**
     * Value 类型
     */
    private final String valueType;
    /**
     * 超时类型
     */
    private final Integer timeoutType;
    /**
     * 过期时间
     */
    private final Integer timeout;

}
