package cn.iocoder.yudao.framework.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Cache 配置项
 *
 * @author
 */
@ConfigurationProperties("yudao.cache")
@Data
@Validated
public class YudaoCacheProperties {

    /**
     * redis scan 一次返回数量
     */
    private Integer redisScanBatchSize = 30;
}
