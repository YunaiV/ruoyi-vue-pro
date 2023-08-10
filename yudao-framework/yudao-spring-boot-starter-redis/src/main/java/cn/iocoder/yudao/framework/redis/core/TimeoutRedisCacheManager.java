package cn.iocoder.yudao.framework.redis.core;

import cn.hutool.core.util.StrUtil;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * 支持自定义过期时间的 {@link RedisCacheManager} 实现类
 *
 * 在 {@link Cacheable#cacheNames()} 格式为 "key#ttl" 时，# 后面的 ttl 为过期时间，单位为秒
 *
 * @author 芋道源码
 */
public class TimeoutRedisCacheManager extends RedisCacheManager {

    private static final String SPLIT = "#";

    public TimeoutRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
    }

    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        if (StrUtil.isEmpty(name)) {
            return super.createRedisCache(name, cacheConfig);
        }
        // 如果使用 # 分隔，大小不为 2，则说明不使用自定义过期时间
        String[] names = StrUtil.splitToArray(name, SPLIT);
        if (names.length != 2) {
            return super.createRedisCache(name, cacheConfig);
        }

        // 核心：通过修改 cacheConfig 的过期时间，实现自定义过期时间
        if (cacheConfig != null) {
            // 移除 # 后面的 : 以及后面的内容，避免影响解析
            names[1] = StrUtil.subBefore(names[1], StrUtil.COLON, false);
            // 解析时间
            Duration duration = DurationStyle.detectAndParse(names[1], ChronoUnit.SECONDS);
            cacheConfig = cacheConfig.entryTtl(duration);
        }
        return super.createRedisCache(names[0], cacheConfig);
    }

}
