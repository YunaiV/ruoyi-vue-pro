package cn.iocoder.yudao.module.iot.gateway.protocol.tcp;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolInstanceProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpFrameCodec;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpFrameCodecFactory;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.handler.downstream.IotTcpDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.handler.downstream.IotTcpDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.handler.upstream.IotTcpUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.IotTcpConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializerManager;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * IoT TCP 协议实现
 * <p>
 * 基于 Vert.x 实现 TCP 服务器，接收设备上行消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotTcpProtocol implements IotProtocol {

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
     * TCP 服务器
     */
    private NetServer tcpServer;
    /**
     * TCP 连接管理器
     */
    private final IotTcpConnectionManager connectionManager;

    /**
     * 下行消息订阅者
     */
    private final IotTcpDownstreamSubscriber downstreamSubscriber;

    /**
     * 消息序列化器
     */
    private final IotMessageSerializer serializer;
    /**
     * TCP 帧编解码器
     */
    private final IotTcpFrameCodec frameCodec;

    public IotTcpProtocol(ProtocolInstanceProperties properties) {
        IotTcpConfig tcpConfig = properties.getTcp();
        Assert.notNull(tcpConfig, "TCP 协议配置（tcp）不能为空");
        Assert.notNull(tcpConfig.getCodec(), "TCP 拆包配置（tcp.codec）不能为空");
        this.properties = properties;
        this.serverId = IotDeviceMessageUtils.generateServerId(properties.getPort());

        // 初始化序列化器
        IotSerializeTypeEnum serializeType = IotSerializeTypeEnum.of(properties.getSerialize());
        Assert.notNull(serializeType, "不支持的序列化类型：" + properties.getSerialize());
        IotMessageSerializerManager serializerManager = SpringUtil.getBean(IotMessageSerializerManager.class);
        this.serializer = serializerManager.get(serializeType);
        // 初始化帧编解码器
        this.frameCodec = IotTcpFrameCodecFactory.create(tcpConfig.getCodec());

        // 初始化连接管理器
        this.connectionManager = new IotTcpConnectionManager(tcpConfig.getMaxConnections());

        // 初始化下行消息订阅者
        IotMessageBus messageBus = SpringUtil.getBean(IotMessageBus.class);
        IotTcpDownstreamHandler downstreamHandler = new IotTcpDownstreamHandler(connectionManager, frameCodec, serializer);
        this.downstreamSubscriber = new IotTcpDownstreamSubscriber(this, downstreamHandler, messageBus);
    }

    @Override
    public String getId() {
        return properties.getId();
    }

    @Override
    public IotProtocolTypeEnum getType() {
        return IotProtocolTypeEnum.TCP;
    }

    @Override
    public void start() {
        if (running) {
            log.warn("[start][IoT TCP 协议 {} 已经在运行中]", getId());
            return;
        }

        // 1.1 创建 Vertx 实例
        this.vertx = Vertx.vertx();

        // 1.2 创建服务器选项
        IotTcpConfig tcpConfig = properties.getTcp();
        NetServerOptions options = new NetServerOptions()
                .setPort(properties.getPort())
                .setTcpKeepAlive(true)
                .setTcpNoDelay(true)
                .setReuseAddress(true)
                .setIdleTimeout((int) (tcpConfig.getKeepAliveTimeoutMs() / 1000)); // 设置空闲超时
        if (Boolean.TRUE.equals(tcpConfig.getSslEnabled())) {
            PemKeyCertOptions pemKeyCertOptions = new PemKeyCertOptions()
                    .setKeyPath(tcpConfig.getSslKeyPath())
                    .setCertPath(tcpConfig.getSslCertPath());
            options.setSsl(true).setKeyCertOptions(pemKeyCertOptions);
        }

        // 1.3 创建服务器并设置连接处理器
        tcpServer = vertx.createNetServer(options);
        tcpServer.connectHandler(socket -> {
            IotTcpUpstreamHandler handler = new IotTcpUpstreamHandler(serverId, frameCodec, serializer, connectionManager);
            handler.handle(socket);
        });

        // 1.4 启动 TCP 服务器
        try {
            tcpServer.listen().result();
            running = true;
            log.info("[start][IoT TCP 协议 {} 启动成功，端口：{}，serverId：{}]",
                    getId(), properties.getPort(), serverId);

            // 2. 启动下行消息订阅者
            this.downstreamSubscriber.start();
        } catch (Exception e) {
            log.error("[start][IoT TCP 协议 {} 启动失败]", getId(), e);
            // 启动失败时关闭资源
            if (tcpServer != null) {
                tcpServer.close();
                tcpServer = null;
            }
            if (vertx != null) {
                vertx.close();
                vertx = null;
            }
            throw e;
        }
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        // 1. 停止下行消息订阅者
        try {
            downstreamSubscriber.stop();
            log.info("[stop][IoT TCP 协议 {} 下行消息订阅者已停止]", getId());
        } catch (Exception e) {
            log.error("[stop][IoT TCP 协议 {} 下行消息订阅者停止失败]", getId(), e);
        }

        // 2.1 关闭 TCP 服务器
        if (tcpServer != null) {
            try {
                tcpServer.close().result();
                log.info("[stop][IoT TCP 协议 {} 服务器已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT TCP 协议 {} 服务器停止失败]", getId(), e);
            }
            tcpServer = null;
        }
        // 2.2 关闭 Vertx 实例
        if (vertx != null) {
            try {
                vertx.close().result();
                log.info("[stop][IoT TCP 协议 {} Vertx 已关闭]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT TCP 协议 {} Vertx 关闭失败]", getId(), e);
            }
            vertx = null;
        }
        running = false;
        log.info("[stop][IoT TCP 协议 {} 已停止]", getId());
    }

}
