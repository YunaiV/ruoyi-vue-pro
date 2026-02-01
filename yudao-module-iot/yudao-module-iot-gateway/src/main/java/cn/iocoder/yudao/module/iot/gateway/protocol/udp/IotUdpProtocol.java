package cn.iocoder.yudao.module.iot.gateway.protocol.udp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolInstanceProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.handler.downstream.IotUdpDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.handler.downstream.IotUdpDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.handler.upstream.IotUdpUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.manager.IotUdpSessionManager;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializerManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.List;

/**
 * IoT UDP 协议实现
 * <p>
 * 基于 Vert.x 实现 UDP 服务器，接收设备上行消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotUdpProtocol implements IotProtocol {

    /**
     * 协议配置
     */
    private final ProtocolInstanceProperties properties;
    /**
     * 服务器 ID（用于消息追踪，全局唯一）
     */
    @Getter
    private final String serverId;

    /**
     * 运行状态
     */
    @Getter
    private volatile boolean running = false;

    /**
     * Vert.x 实例
     */
    private Vertx vertx;
    /**
     * UDP 服务器
     */
    @Getter
    private DatagramSocket udpSocket;

    /**
     * 下行消息订阅者
     */
    private final IotUdpDownstreamSubscriber downstreamSubscriber;

    /**
     * 消息序列化器
     */
    private final IotMessageSerializer serializer;

    /**
     * UDP 会话管理器
     */
    private final IotUdpSessionManager sessionManager;

    private final IotDeviceService deviceService;
    private final IotDeviceMessageService deviceMessageService;

    /**
     * 会话清理定时器 ID
     */
    // TODO @AI：会话清理，是不是放到 sessionManager 更合适？
    private Long cleanTimerId;

    public IotUdpProtocol(ProtocolInstanceProperties properties) {
        IotUdpConfig udpConfig = properties.getUdp();
        Assert.notNull(udpConfig, "UDP 协议配置（udp）不能为空");
        this.properties = properties;
        this.serverId = IotDeviceMessageUtils.generateServerId(properties.getPort());
        this.deviceService = SpringUtil.getBean(IotDeviceService.class);
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);

        // 初始化序列化器
        IotSerializeTypeEnum serializeType = IotSerializeTypeEnum.of(properties.getSerialize());
        Assert.notNull(serializeType, "不支持的序列化类型：" + properties.getSerialize());
        IotMessageSerializerManager serializerManager = SpringUtil.getBean(IotMessageSerializerManager.class);
        this.serializer = serializerManager.get(serializeType);

        // 初始化会话管理器
        this.sessionManager = new IotUdpSessionManager(udpConfig.getMaxSessions());

        // 初始化下行消息订阅者
        IotMessageBus messageBus = SpringUtil.getBean(IotMessageBus.class);
        IotUdpDownstreamHandler downstreamHandler = new IotUdpDownstreamHandler(this, sessionManager, serializer);
        this.downstreamSubscriber = new IotUdpDownstreamSubscriber(this, downstreamHandler, messageBus);
    }

    @Override
    public String getId() {
        return properties.getId();
    }

    @Override
    public IotProtocolTypeEnum getType() {
        return IotProtocolTypeEnum.UDP;
    }

    @Override
    public void start() {
        if (running) {
            log.warn("[start][IoT UDP 协议 {} 已经在运行中]", getId());
            return;
        }

        // 1.1 创建 Vertx 实例
        this.vertx = Vertx.vertx();

        // 1.2 创建 UDP Socket 选项
        IotUdpConfig udpConfig = properties.getUdp();
        DatagramSocketOptions options = new DatagramSocketOptions()
                .setReceiveBufferSize(udpConfig.getReceiveBufferSize())
                .setSendBufferSize(udpConfig.getSendBufferSize())
                .setReuseAddress(true);

        // 1.3 创建 UDP Socket
        udpSocket = vertx.createDatagramSocket(options);

        // 1.4 创建上行消息处理器
        IotUdpUpstreamHandler upstreamHandler = new IotUdpUpstreamHandler(serverId, sessionManager, serializer);

        // 1.5 监听端口
        udpSocket.listen(properties.getPort(), "0.0.0.0", result -> {
            if (result.failed()) {
                log.error("[start][IoT UDP 协议 {} 启动失败]", getId(), result.cause());
                return;
            }
            // 设置数据包处理器
            udpSocket.handler(packet -> upstreamHandler.handle(packet, udpSocket));
            running = true;
            log.info("[start][IoT UDP 协议 {} 启动成功，端口：{}，serverId：{}]",
                    getId(), properties.getPort(), serverId);
            // 启动会话清理定时器
            startSessionCleanTimer(udpConfig);

            // 2. 启动下行消息订阅者
            // TODO @AI：这里会导致  Thread Thread[vert.x-eventloop-thread-0,5,main] has been blocked for 2992 ms, time limit is 2000 ms
            this.downstreamSubscriber.start();
        });
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        // 1. 停止下行消息订阅者
        try {
            downstreamSubscriber.stop();
            log.info("[stop][IoT UDP 协议 {} 下行消息订阅者已停止]", getId());
        } catch (Exception e) {
            log.error("[stop][IoT UDP 协议 {} 下行消息订阅者停止失败]", getId(), e);
        }

        // 2.1 取消会话清理定时器
        if (cleanTimerId != null) {
            vertx.cancelTimer(cleanTimerId);
            cleanTimerId = null;
            log.info("[stop][IoT UDP 协议 {} 会话清理定时器已取消]", getId());
        }
        // 2.2 关闭 UDP Socket
        if (udpSocket != null) {
            try {
                udpSocket.close().result();
                log.info("[stop][IoT UDP 协议 {} 服务器已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT UDP 协议 {} 服务器停止失败]", getId(), e);
            }
            udpSocket = null;
        }
        // 2.3 关闭 Vertx 实例
        if (vertx != null) {
            try {
                vertx.close().result();
                log.info("[stop][IoT UDP 协议 {} Vertx 已关闭]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT UDP 协议 {} Vertx 关闭失败]", getId(), e);
            }
            vertx = null;
        }
        running = false;
        log.info("[stop][IoT UDP 协议 {} 已停止]", getId());
    }

    /**
     * 启动会话清理定时器
     */
    // TODO @AI：这个放到
    private void startSessionCleanTimer(IotUdpConfig udpConfig) {
        cleanTimerId = vertx.setPeriodic(udpConfig.getSessionCleanIntervalMs(), id -> {
            try {
                // 1. 清理超时的设备会话，并获取离线设备列表
                List<Long> offlineDeviceIds = sessionManager.cleanExpiredSessions(udpConfig.getSessionTimeoutMs());

                // 2. 为每个离线设备发送离线消息
                for (Long deviceId : offlineDeviceIds) {
                    sendOfflineMessage(deviceId);
                }
                if (CollUtil.isNotEmpty(offlineDeviceIds)) {
                    log.info("[cleanExpiredSessions][本次清理 {} 个超时设备]", offlineDeviceIds.size());
                }
            } catch (Exception e) {
                log.error("[cleanExpiredSessions][清理超时会话失败]", e);
            }
        });
        log.info("[startSessionCleanTimer][会话清理定时器启动，间隔：{} ms，超时：{} ms]",
                udpConfig.getSessionCleanIntervalMs(), udpConfig.getSessionTimeoutMs());
    }

    /**
     * 发送设备离线消息
     *
     * @param deviceId 设备 ID
     */
    private void sendOfflineMessage(Long deviceId) {
        try {
            // 获取设备信息
            IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceId);
            if (device == null) {
                log.warn("[sendOfflineMessage][设备不存在，设备 ID: {}]", deviceId);
                return;
            }

            // 发送离线消息
            IotDeviceMessage offlineMessage = IotDeviceMessage.buildStateOffline();
            deviceMessageService.sendDeviceMessage(offlineMessage, device.getProductKey(),
                    device.getDeviceName(), serverId);
            log.info("[sendOfflineMessage][发送离线消息，设备 ID: {}，设备名: {}]",
                    deviceId, device.getDeviceName());
        } catch (Exception e) {
            log.error("[sendOfflineMessage][发送离线消息失败，设备 ID: {}]", deviceId, e);
        }
    }

}
