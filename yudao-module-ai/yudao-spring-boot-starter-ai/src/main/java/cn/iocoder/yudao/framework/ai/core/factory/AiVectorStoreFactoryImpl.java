package cn.iocoder.yudao.framework.ai.core.factory;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.lang.func.Func0;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import redis.clients.jedis.JedisPooled;

/**
 * AI Vector 模型工厂的实现类
 * 使用 redisVectorStore 实现 VectorStore
 *
 * @author xiaoxin
 */
public class AiVectorStoreFactoryImpl implements AiVectorStoreFactory {

    @Override
    public VectorStore getOrCreateVectorStore(EmbeddingModel embeddingModel, AiPlatformEnum platform, String apiKey, String url) {
        String cacheKey = buildClientCacheKey(VectorStore.class, platform, apiKey, url);
        return Singleton.get(cacheKey, (Func0<VectorStore>) () -> {
            // TODO 芋艿 @xin 这两个配置取哪好呢
            // TODO 不同模型的向量维度可能会不一样，目前看貌似是以 index 来做区分的，维度不一样存不到一个 index 上
            String index = "default-index";
            String prefix = "default:";
            var config = RedisVectorStore.RedisVectorStoreConfig.builder()
                    .withIndexName(index)
                    .withPrefix(prefix)
                    .build();
            RedisProperties redisProperties = SpringUtils.getBean(RedisProperties.class);
            RedisVectorStore redisVectorStore = new RedisVectorStore(config, embeddingModel,
                    new JedisPooled(redisProperties.getHost(), redisProperties.getPort()),
                    true);
            redisVectorStore.afterPropertiesSet();
            return redisVectorStore;
        });
    }


    private static String buildClientCacheKey(Class<?> clazz, Object... params) {
        if (ArrayUtil.isEmpty(params)) {
            return clazz.getName();
        }
        return StrUtil.format("{}#{}", clazz.getName(), ArrayUtil.join(params, "_"));
    }
}
