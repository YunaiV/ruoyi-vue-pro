package cn.iocoder.yudao.module.iot.gateway.protocol.tcp;

import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.IotTcpSessionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router.IotTcpUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 TCP 协议：接收设备上行消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotTcpUpstreamProtocol {

    private final IotGatewayProperties.TcpProperties tcpProperties;

    private final IotDeviceService deviceService;

    private final IotDeviceMessageService messageService;

    private final IotTcpSessionManager sessionManager;

    private final Vertx vertx;

    @Getter
    private final String serverId;

    private NetServer netServer;

    public IotTcpUpstreamProtocol(IotGatewayProperties.TcpProperties tcpProperties,
            IotDeviceService deviceService,
            IotDeviceMessageService messageService,
                                  IotTcpSessionManager sessionManager,
            Vertx vertx) {
        this.tcpProperties = tcpProperties;
        this.deviceService = deviceService;
        this.messageService = messageService;
        this.sessionManager = sessionManager;
        this.vertx = vertx;
        this.serverId = IotDeviceMessageUtils.generateServerId(tcpProperties.getPort());
    }

    @PostConstruct
    public void start() {
        // 创建服务器选项
        NetServerOptions options = new NetServerOptions()
                .setPort(tcpProperties.getPort())
                .setTcpKeepAlive(true)
                .setTcpNoDelay(true)
                .setReuseAddress(true);
        // 配置 SSL（如果启用）
        if (Boolean.TRUE.equals(tcpProperties.getSslEnabled())) {
            PemKeyCertOptions pemKeyCertOptions = new PemKeyCertOptions()
                    .setKeyPath(tcpProperties.getSslKeyPath())
                    .setCertPath(tcpProperties.getSslCertPath());
            options.setSsl(true).setKeyCertOptions(pemKeyCertOptions);
        }

        // 创建服务器并设置连接处理器
        netServer = vertx.createNetServer(options);
        netServer.connectHandler(socket -> {
            IotTcpUpstreamHandler handler = new IotTcpUpstreamHandler(this, messageService, deviceService,
                    sessionManager);
            handler.handle(socket);
        });

        // 启动服务器
        try {
            netServer.listen().result();
            log.info("[start][IoT 网关 TCP 协议启动成功，端口：{}]", tcpProperties.getPort());
        } catch (Exception e) {
            log.error("[start][IoT 网关 TCP 协议启动失败]", e);
            throw e;
        }
    }

    @PreDestroy
    public void stop() {
        if (netServer != null) {
            try {
                netServer.close().result();
                log.info("[stop][IoT 网关 TCP 协议已停止]");
            } catch (Exception e) {
                log.error("[stop][IoT 网关 TCP 协议停止失败]", e);
            }
        }
    }
}