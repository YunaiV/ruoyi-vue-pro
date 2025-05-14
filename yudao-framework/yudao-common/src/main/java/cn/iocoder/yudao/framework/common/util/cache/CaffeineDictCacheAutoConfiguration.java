package cn.iocoder.yudao.framework.common.util.cache;

import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 字典缓存配置类
 *
 * @author Ken
 */
@AutoConfiguration
@EnableConfigurationProperties(CaffeineDictCacheProperties.class)
@Slf4j
public class CaffeineDictCacheAutoConfiguration {

    /**
     * 初始化字典缓存
     */
    @Bean
    public CaffeineDictCacheInitializer caffeineDictCacheInitializer(
            CaffeineDictCacheProperties properties) {
        return new CaffeineDictCacheInitializer(properties);
    }

    @Slf4j
    public static class CaffeineDictCacheInitializer {

        private final CaffeineDictCacheProperties properties;

        public CaffeineDictCacheInitializer(CaffeineDictCacheProperties properties) {
            this.properties = properties;
            this.initialize();
        }

        /**
         * 初始化缓存
         */
        private void initialize() {
            if (!properties.isWarmupEnabled()) {
                log.info("字典缓存预热功能未开启");
                return;
            }

            // 设置默认SQL
            if (!StringUtils.hasText(properties.getDefaultSql())) {
                log.warn("未配置默认的字典查询SQL");
                return;
            }
            CaffeineDictCache.setDictQuerySql(properties.getDefaultSql());

            // 初始化所有配置的场景
            Map<String, List<String>> dictTypes = properties.getWarmupDictTypes();
            Map<String, String> sqls = properties.getWarmupSqls();

            for (Map.Entry<String, List<String>> entry : dictTypes.entrySet()) {
                String scene = entry.getKey();
                List<String> types = entry.getValue();
                String sql = sqls.getOrDefault(scene, properties.getDefaultSql());

                try {
                    log.info("开始预热场景 [{}] 的字典缓存，字典类型: {}", scene, types);
                    CaffeineDictCache.warmup(types, sql);
                    log.info("场景 [{}] 的字典缓存预热完成", scene);
                } catch (Exception e) {
                    log.error("场景 [{}] 的字典缓存预热失败", scene, e);
                }
            }
        }
    }
}
