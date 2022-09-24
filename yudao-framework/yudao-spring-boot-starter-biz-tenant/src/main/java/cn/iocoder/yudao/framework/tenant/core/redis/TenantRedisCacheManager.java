package cn.iocoder.yudao.framework.tenant.core.redis;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

/**
 * 多租户的 {@link RedisCacheManager} 实现类
 *
 * 操作指定 name 的 {@link Cache} 时，自动拼接租户后缀，格式为 name + ":" + tenantId + 后缀
 *
 * @author airhead
 */
@Slf4j
public class TenantRedisCacheManager extends RedisCacheManager {

    public TenantRedisCacheManager(RedisCacheWriter cacheWriter,
                                   RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
    }

    @Override
    public Cache getCache(String name) {
        // 如果开启多租户，则 name 拼接租户后缀
        if (!TenantContextHolder.isIgnore()
            && TenantContextHolder.getTenantId() != null) {
            name = name + ":" + TenantContextHolder.getTenantId();
        }

        // 继续基于父方法
        return super.getCache(name);
    }

}
