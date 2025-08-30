package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataSinkDO;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

// TODO @芋艿：数据库
// TODO @芋艿：mqtt
// TODO @芋艿：tcp
// TODO @芋艿：websocket

/**
 * 可缓存的 {@link IotDataRuleAction} 抽象实现
 *
 * 该类提供了一个通用的缓存机制，用于管理各类数据桥接的生产者(Producer)实例。
 *
 * 主要特点:
 * - 基于Guava Cache实现高效的生产者实例缓存管理
 * - 自动处理生产者的生命周期（创建、获取、关闭）
 * - 支持 30 分钟未访问自动过期清理机制
 * - 异常处理与日志记录，便于问题排查
 *
 * 子类需要实现:
 * - initProducer(Config) - 初始化特定类型的生产者实例
 * - closeProducer(Producer) - 关闭生产者实例并释放资源
 *
 * @param <Config>   配置信息类型，用于初始化生产者
 * @param <Producer> 生产者类型，负责将数据发送到目标系统
 * @author HUIHUI
 */
@Slf4j
public abstract class IotDataRuleCacheableAction<Config, Producer> implements IotDataRuleAction {

    /**
     * Producer 缓存
     */
    private final LoadingCache<Config, Producer> PRODUCER_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30)) // 30 分钟未访问就提前过期
            .removalListener((RemovalListener<Config, Producer>) notification -> {
                Producer producer = notification.getValue();
                try {
                    closeProducer(producer);
                    log.info("[PRODUCER_CACHE][配置({}) 对应的 producer 已关闭]", notification.getKey());
                } catch (Exception e) {
                    log.error("[PRODUCER_CACHE][配置({}) 对应的 producer 关闭失败]", notification.getKey(), e);
                }
            })
            .build(new CacheLoader<Config, Producer>() {

                @Override
                public Producer load(Config config) throws Exception {
                    try {
                        Producer producer = initProducer(config);
                        log.info("[PRODUCER_CACHE][配置({}) 对应的 producer 已创建并启动]", config);
                        return producer;
                    } catch (Exception e) {
                        log.error("[PRODUCER_CACHE][配置({}) 对应的 producer 创建启动失败]", config, e);
                        throw e; // 抛出异常，触发缓存加载失败机制
                    }
                }

            });

    /**
     * 获取生产者
     *
     * @param config 配置信息
     * @return 生产者对象
     */
    protected Producer getProducer(Config config) throws Exception {
        return PRODUCER_CACHE.get(config);
    }

    /**
     * 初始化生产者
     *
     * @param config 配置信息
     * @return 生产者对象
     * @throws Exception 如果初始化失败
     */
    protected abstract Producer initProducer(Config config) throws Exception;

    /**
     * 关闭生产者
     *
     * @param producer 生产者对象
     */
    protected abstract void closeProducer(Producer producer) throws Exception;

    @Override
    @SuppressWarnings({"unchecked"})
    public void execute(IotDeviceMessage message, IotDataSinkDO dataSink) {
        Assert.isTrue(ObjUtil.equal(dataSink.getType(), getType()), "类型({})不匹配", dataSink.getType());
        try {
            execute(message, (Config) dataSink.getConfig());
        } catch (Exception e) {
            log.error("[execute][桥梁配置 config({}) 对应的 message({}) 发送异常]", dataSink.getConfig(), message, e);
        }
    }

    /**
     * 执行数据流转
     *
     * @param message 设备消息
     * @param config  配置信息
     */
    protected abstract void execute(IotDeviceMessage message, Config config) throws Exception;

}