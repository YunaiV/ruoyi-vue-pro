package cn.iocoder.yudao.module.iot.gateway.protocol.udp;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.handler.downstream.IotUdpDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.handler.downstream.IotUdpDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.handler.upstream.IotUdpUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.manager.IotUdpSessionManager;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializerManager;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.lang.Assert;

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
    private final ProtocolProperties properties;
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
     * UDP 会话管理器
     */
    private final IotUdpSessionManager sessionManager;

    /**
     * 下行消息订阅者
     */
    private IotUdpDownstreamSubscriber downstreamSubscriber;

    /**
     * 消息序列化器
     */
    private final IotMessageSerializer serializer;

    public IotUdpProtocol(ProtocolProperties properties) {
        IotUdpConfig udpConfig = properties.getUdp();
        Assert.notNull(udpConfig, "UDP 协议配置（udp）不能为空");
        this.properties = properties;
        this.serverId = IotDeviceMessageUtils.generateServerId(properties.getPort());

        // 初始化序列化器
        IotSerializeTypeEnum serializeType = IotSerializeTypeEnum.of(properties.getSerialize());
        Assert.notNull(serializeType, "不支持的序列化类型：" + properties.getSerialize());
        IotMessageSerializerManager serializerManager = SpringUtil.getBean(IotMessageSerializerManager.class);
        this.serializer = serializerManager.get(serializeType);

        // 初始化会话管理器
        this.sessionManager = new IotUdpSessionManager(udpConfig.getMaxSessions(), udpConfig.getSessionTimeoutMs());

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

        // 1.1 创建 Vertx 实例 和 下行消息订阅者
        this.vertx = Vertx.vertx();
        IotMessageBus messageBus = SpringUtil.getBean(IotMessageBus.class);
        IotUdpDownstreamHandler downstreamHandler = new IotUdpDownstreamHandler(this, sessionManager, serializer);
        this.downstreamSubscriber = new IotUdpDownstreamSubscriber(this, downstreamHandler, messageBus);

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

        // 1.5 启动 UDP 服务器（阻塞式）
        try {
            udpSocket.listen(properties.getPort(), "0.0.0.0").result();
            // 设置数据包处理器
            udpSocket.handler(packet -> upstreamHandler.handle(packet, udpSocket));
            running = true;
            log.info("[start][IoT UDP 协议 {} 启动成功，端口：{}，serverId：{}]",
                    getId(), properties.getPort(), serverId);

            // 2. 启动下行消息订阅者
            this.downstreamSubscriber.start();
        } catch (Exception e) {
            log.error("[start][IoT UDP 协议 {} 启动失败]", getId(), e);
            stop0();
            throw e;
        }
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        stop0();
    }

    private void stop0() {
        // 1. 停止下行消息订阅者
        if (downstreamSubscriber != null) {
            try {
                downstreamSubscriber.stop();
                log.info("[stop][IoT UDP 协议 {} 下行消息订阅者已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT UDP 协议 {} 下行消息订阅者停止失败]", getId(), e);
            }
            downstreamSubscriber = null;
        }

        // 2.1 关闭 UDP Socket
        if (udpSocket != null) {
            try {
                udpSocket.close().result();
                log.info("[stop][IoT UDP 协议 {} 服务器已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT UDP 协议 {} 服务器停止失败]", getId(), e);
            }
            udpSocket = null;
        }
        // 2.2 关闭 Vertx 实例
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

}
