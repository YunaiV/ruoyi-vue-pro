package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkWebSocketConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.data.action.websocket.IotWebSocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * WebSocket 的 {@link IotDataRuleAction} 实现类
 * <p>
 * 负责将设备消息发送到外部 WebSocket 服务器
 * 支持 ws:// 和 wss:// 协议，支持 JSON 和 TEXT 数据格式
 * 使用连接池管理 WebSocket 连接，提高性能和资源利用率
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotWebSocketDataRuleAction extends
        IotDataRuleCacheableAction<IotDataSinkWebSocketConfig, IotWebSocketClient> {

    /**
     * 锁等待超时时间（毫秒）
     */
    private static final long LOCK_WAIT_TIME_MS = 5000;

    /**
     * 重连锁，key 为 WebSocket 服务器地址
     * <p>
     * WebSocket 连接是与特定服务器实例绑定的，使用单机锁即可保证重连的线程安全
     */
    private final ConcurrentHashMap<String, ReentrantLock> reconnectLocks = new ConcurrentHashMap<>();

    @Override
    public Integer getType() {
        return IotDataSinkTypeEnum.WEBSOCKET.getType();
    }

    @Override
    protected IotWebSocketClient initProducer(IotDataSinkWebSocketConfig config) throws Exception {
        // 1. 参数校验
        if (StrUtil.isBlank(config.getServerUrl())) {
            throw new IllegalArgumentException("WebSocket 服务器地址不能为空");
        }
        if (!StrUtil.startWithAny(config.getServerUrl(), "ws://", "wss://")) {
            throw new IllegalArgumentException("WebSocket 服务器地址必须以 ws:// 或 wss:// 开头");
        }

        // 2.1 创建 WebSocket 客户端
        IotWebSocketClient webSocketClient = new IotWebSocketClient(
                config.getServerUrl(),
                config.getConnectTimeoutMs(),
                config.getSendTimeoutMs(),
                config.getDataFormat()
        );
        // 2.2 连接服务器
        webSocketClient.connect();
        log.info("[initProducer][WebSocket 客户端创建并连接成功，服务器: {}，数据格式: {}]",
                config.getServerUrl(), config.getDataFormat());
        return webSocketClient;
    }

    @Override
    protected void closeProducer(IotWebSocketClient producer) throws Exception {
        if (producer != null) {
            producer.close();
        }
    }

    @Override
    protected void execute(IotDeviceMessage message, IotDataSinkWebSocketConfig config) throws Exception {
        try {
            // 1.1 获取或创建 WebSocket 客户端
            IotWebSocketClient webSocketClient = getProducer(config);

            // 1.2 检查连接状态，如果断开则使用分布式锁保证重连的线程安全
            if (!webSocketClient.isConnected()) {
                reconnectWithLock(webSocketClient, config);
            }

            // 2.1 发送消息
            webSocketClient.sendMessage(message);
            // 2.2 记录发送成功日志
            log.info("[execute][message({}) config({}) 发送成功，WebSocket 服务器: {}]",
                    message, config, config.getServerUrl());
        } catch (Exception e) {
            log.error("[execute][message({}) config({}) 发送失败，WebSocket 服务器: {}]",
                    message, config, config.getServerUrl(), e);
            throw e;
        }
    }

    // TODO @puhui999：为什么这里要加锁呀？
    /**
     * 使用锁进行重连，保证同一服务器地址的重连操作线程安全
     *
     * @param webSocketClient WebSocket 客户端
     * @param config          配置信息
     */
    private void reconnectWithLock(IotWebSocketClient webSocketClient, IotDataSinkWebSocketConfig config) throws Exception {
        ReentrantLock lock = reconnectLocks.computeIfAbsent(config.getServerUrl(), k -> new ReentrantLock());
        boolean acquired = false;
        try {
            acquired = lock.tryLock(LOCK_WAIT_TIME_MS, TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new RuntimeException("获取 WebSocket 重连锁超时，服务器: " + config.getServerUrl());
            }
            // 双重检查：获取锁后再次检查连接状态，避免重复连接
            if (!webSocketClient.isConnected()) {
                log.warn("[reconnectWithLock][WebSocket 连接已断开，尝试重新连接，服务器: {}]", config.getServerUrl());
                webSocketClient.connect();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取 WebSocket 重连锁被中断，服务器: " + config.getServerUrl(), e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
