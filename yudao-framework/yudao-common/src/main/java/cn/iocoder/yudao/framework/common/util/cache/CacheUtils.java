package cn.iocoder.yudao.framework.common.util.cache;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Cache 工具类
 *
 * @author 芋道源码
 */
public class CacheUtils {

    public static <K, V> LoadingCache<K, V> buildAsyncReloadingCache(Duration duration, CacheLoader<K, V> loader) {
        Executor executor = Executors.newCachedThreadPool(  // TODO 芋艿：可能要思考下，未来要不要做成可配置
                TtlExecutors.getDefaultDisableInheritableThreadFactory()); // TTL 保证 ThreadLocal 可以透传
        return CacheBuilder.newBuilder()
                // 只阻塞当前数据加载线程，其他线程返回旧值
                .refreshAfterWrite(duration)
                // 通过 asyncReloading 实现全异步加载，包括 refreshAfterWrite 被阻塞的加载线程
                .build(CacheLoader.asyncReloading(loader, executor));
    }

}
