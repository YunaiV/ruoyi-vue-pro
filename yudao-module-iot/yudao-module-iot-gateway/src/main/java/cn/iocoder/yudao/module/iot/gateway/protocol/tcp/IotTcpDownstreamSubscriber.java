package cn.iocoder.yudao.module.iot.gateway.protocol.tcp;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.TcpDeviceConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router.IotTcpDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IoT 网关 TCP 下游订阅者：接收下行给设备的消息
 * <p>
 * 参考 EMQX 设计理念：
 * 1. 高性能消息路由
 * 2. 容错机制
 * 3. 状态监控
 * 4. 资源管理
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotTcpDownstreamSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    private final IotTcpUpstreamProtocol protocolHandler;

    private final TcpDeviceConnectionManager connectionManager;

    private final IotDeviceMessageService messageService;

    private final IotMessageBus messageBus;

    private volatile IotTcpDownstreamHandler downstreamHandler;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private final AtomicLong processedMessages = new AtomicLong(0);

    private final AtomicLong failedMessages = new AtomicLong(0);

    @PostConstruct
    public void init() {
        if (!initialized.compareAndSet(false, true)) {
            log.warn("[init][TCP 下游消息订阅者已初始化，跳过重复初始化]");
            return;
        }

        try {
            // 初始化下游处理器
            downstreamHandler = new IotTcpDownstreamHandler(connectionManager, messageService);

            // 注册到消息总线
            messageBus.register(this);

            log.info("[init][TCP 下游消息订阅者初始化完成] Topic: {}, Group: {}",
                    getTopic(), getGroup());
        } catch (Exception e) {
            initialized.set(false);
            log.error("[init][TCP 下游消息订阅者初始化失败]", e);
            throw new RuntimeException("TCP 下游消息订阅者初始化失败", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (!initialized.get()) {
            return;
        }

        try {
            log.info("[destroy][TCP 下游消息订阅者已关闭] 处理消息数: {}, 失败消息数: {}",
                    processedMessages.get(), failedMessages.get());
        } catch (Exception e) {
            log.error("[destroy][TCP 下游消息订阅者关闭失败]", e);
        } finally {
            initialized.set(false);
        }
    }

    @Override
    public String getTopic() {
        return IotDeviceMessageUtils.buildMessageBusGatewayDeviceMessageTopic(protocolHandler.getServerId());
    }

    @Override
    public String getGroup() {
        return "tcp-downstream-" + protocolHandler.getServerId();
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        if (!initialized.get()) {
            log.warn("[onMessage][订阅者未初始化，跳过消息处理]");
            return;
        }

        long startTime = System.currentTimeMillis();

        try {
            processedMessages.incrementAndGet();

            if (log.isDebugEnabled()) {
                log.debug("[onMessage][收到下行消息] 设备 ID: {}, 方法: {}, 消息ID: {}",
                        message.getDeviceId(), message.getMethod(), message.getId());
            }
            // 参数校验
            if (message.getDeviceId() == null) {
                log.warn("[onMessage][下行消息设备 ID 为空，跳过处理] 消息: {}", message);
                return;
            }
            // 检查连接状态
            if (connectionManager.getClientByDeviceId(message.getDeviceId()) == null) {
                log.warn("[onMessage][设备({})离线，跳过下行消息] 方法: {}",
                        message.getDeviceId(), message.getMethod());
                return;
            }

            // 处理下行消息
            downstreamHandler.handle(message);

            // 性能监控
            long processTime = System.currentTimeMillis() - startTime;
            // TODO @haohao：1000 搞成静态变量；
            if (processTime > 1000) { // 超过 1 秒的慢消息
                log.warn("[onMessage][慢消息处理] 设备ID: {}, 方法: {}, 耗时: {}ms",
                        message.getDeviceId(), message.getMethod(), processTime);
            }
        } catch (Exception e) {
            failedMessages.incrementAndGet();
            log.error("[onMessage][处理下行消息失败] 设备ID: {}, 方法: {}, 消息: {}",
                    message.getDeviceId(), message.getMethod(), message, e);
        }
    }

    // TODO @haohao：多余的要不先清理掉；

    /**
     * 获取订阅者统计信息
     */
    public String getSubscriberStatistics() {
        return String.format("TCP下游订阅者 - 已处理: %d, 失败: %d, 成功率: %.2f%%",
                processedMessages.get(),
                failedMessages.get(),
                processedMessages.get() > 0
                        ? (double) (processedMessages.get() - failedMessages.get()) / processedMessages.get() * 100
                        : 0.0);
    }

    /**
     * 检查订阅者健康状态
     */
    public boolean isHealthy() {
        return initialized.get() && downstreamHandler != null;
    }

}