package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

// TODO @芋艿：数据库
// TODO @芋艿：mqtt
// TODO @芋艿：tcp
// TODO @芋艿：websocket

/**
 * 带缓存功能的数据桥梁执行器抽象类
 *
 * @author HUIHUI
 */
@Slf4j
public abstract class AbstractCacheableDataBridgeExecute implements IotDataBridgeExecute {

    // TODO @huihui：AbstractCacheableDataBridgeExecute<Producer> 这样，下面的 Object, Object 就有了类型；另外 IotDataBridgeDO.Config 可以替代一个 Object 哇，
    /**
     * Producer 缓存
     */
    private final LoadingCache<Object, Object> PRODUCER_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30))
            .removalListener(notification -> {
                Object producer = notification.getValue();
                if (producer == null) {
                    return;
                }
                try {
                    closeProducer(producer);
                    log.info("[PRODUCER_CACHE][配置({}) 对应的 producer 已关闭]", notification.getKey());
                } catch (Exception e) {
                    log.error("[PRODUCER_CACHE][配置({}) 对应的 producer 关闭失败]", notification.getKey(), e);
                }
            })
            .build(new CacheLoader<Object, Object>() {

                @Override
                public Object load(Object config) throws Exception {
                    Object producer = initProducer(config);
                    log.info("[PRODUCER_CACHE][配置({}) 对应的 producer 已创建并启动]", config);
                    return producer;
                }

            });

    /**
     * 获取生产者
     *
     * @param config 配置信息
     * @return 生产者对象
     */
    protected Object getProducer(Object config) throws Exception {
        return PRODUCER_CACHE.get(config);
    }

    /**
     * 初始化生产者
     *
     * @param config 配置信息
     * @return 生产者对象
     * @throws Exception 如果初始化失败
     */
    protected abstract Object initProducer(Object config) throws Exception;

    /**
     * 关闭生产者
     *
     * @param producer 生产者对象
     */
    protected abstract void closeProducer(Object producer);

}