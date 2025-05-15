package cn.iocoder.yudao.framework.common.util.cache;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

/**
 * 字典缓存配置类
 *
 * @author Ken
 */
@ConfigurationProperties(prefix = "yudao.dict.cache")
@Data
public class CaffeineDictCacheProperties {

    /**
     * 是否开启缓存预热
     */
    private boolean warmupEnabled = true;

    /**
     * 默认的字典查询SQL
     */
    private String defaultSql;

    /**
     * 缓存预热的字典类型 key: 业务场景标识 value: 需要预热的字典类型列表
     */
    private Map<String, List<String>> warmupDictTypes = Collections.emptyMap();

    /**
     * 缓存预热的SQL key: 业务场景标识 value: SQL语句
     */
    private Map<String, String> warmupSqls = Collections.emptyMap();
}
