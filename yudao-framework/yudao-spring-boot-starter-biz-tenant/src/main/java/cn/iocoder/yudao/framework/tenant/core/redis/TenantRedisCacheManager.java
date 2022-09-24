package cn.iocoder.yudao.framework.tenant.core.redis;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

/**
 * 租户缓存管理
 *
 * 为cacheName增加自动增加租户表示，格式：name+":"+tenantId
 *
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
    //租户未设置时，返回原始name
    if (TenantContextHolder.getTenantId() == null) {
      return super.getCache(name);
    }

    name = name + ":" + TenantContextHolder.getTenantId();
    return super.getCache(name);
  }
}
