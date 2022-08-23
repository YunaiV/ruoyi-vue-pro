package cn.iocoder.yudao.framework.tenant.core.redis;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

/**
 * @author airhead
 */
@Slf4j
public class TenantRedisCacheManager extends RedisCacheManager {
  public TenantRedisCacheManager(
      RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
    super(cacheWriter, defaultCacheConfiguration);
  }

  @Override
  public Cache getCache(String name) {
    if (TenantContextHolder.getTenantId() == null) {
      return super.getCache(name);
    }

    name = name + ":" + TenantContextHolder.getTenantId();
    return super.getCache(name);
  }
}
