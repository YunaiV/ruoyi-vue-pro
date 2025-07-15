package cn.iocoder.yudao.module.iot.gateway.protocol.tcp;

import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.TcpDeviceConnectionManager;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * IoT 网关 TCP 协议：接收设备上行消息
 * <p>
 * 负责接收设备上行消息，支持：
 * 1. 设备注册
 * 2. 心跳保活
 * 3. 属性上报
 * 4. 事件上报
 * 5. 设备连接管理
 *
 * @author 芋道源码
 */
@Slf4j
public class IotTcpUpstreamProtocol {

    private final IotGatewayProperties.TcpProperties tcpProperties;

    private final TcpDeviceConnectionManager connectionManager;

    private final IotDeviceService deviceService;

    private final IotDeviceMessageService messageService;

    private final IotDeviceCommonApi deviceApi;

    private final Vertx vertx;

    @Getter
    private final String serverId;

    private NetServer netServer;

    public IotTcpUpstreamProtocol(IotGatewayProperties.TcpProperties tcpProperties,
                                  TcpDeviceConnectionManager connectionManager,
                                  IotDeviceService deviceService,
                                  IotDeviceMessageService messageService,
                                  IotDeviceCommonApi deviceApi,
                                  Vertx vertx) {
        this.tcpProperties = tcpProperties;
        this.connectionManager = connectionManager;
        this.deviceService = deviceService;
        this.messageService = messageService;
        this.deviceApi = deviceApi;
        this.vertx = vertx;
        this.serverId = IotDeviceMessageUtils.generateServerId(tcpProperties.getPort());
    }

    @PostConstruct
    public void start() {
        // 1. 启动 TCP 服务器
        try {
            startTcpServer();
            log.info("[start][IoT 网关 TCP 协议处理器启动完成，服务器ID: {}]", serverId);
        } catch (Exception e) {
            log.error("[start][IoT 网关 TCP 协议处理器启动失败]", e);
            // 抛出异常，中断 Spring 容器启动
            throw new RuntimeException("IoT 网关 TCP 协议处理器启动失败", e);
        }
    }

    @PreDestroy
    public void stop() {
        if (netServer != null) {
            stopTcpServer();
            log.info("[stop][IoT 网关 TCP 协议处理器已停止]");
        }
    }

    /**
     * 启动 TCP 服务器
     */
    private void startTcpServer() {
        // 1. 创建服务器选项
        NetServerOptions options = new NetServerOptions()
                .setPort(tcpProperties.getPort())
                .setTcpKeepAlive(true)
                .setTcpNoDelay(true)
                .setReuseAddress(true);

        // 2. 配置 SSL（如果启用）
        if (Boolean.TRUE.equals(tcpProperties.getSslEnabled())) {
            PemKeyCertOptions pemKeyCertOptions = new PemKeyCertOptions()
                    .setKeyPath(tcpProperties.getSslKeyPath())
                    .setCertPath(tcpProperties.getSslCertPath());
            options.setSsl(true).setKeyCertOptions(pemKeyCertOptions);
        }

        // 3. 创建 TCP 服务器
        netServer = vertx.createNetServer(options);

        // 4. 设置连接处理器
        netServer.connectHandler(socket -> {
            log.info("[startTcpServer][新设备连接: {}]", socket.remoteAddress());
            IotTcpUpstreamHandler handler = new IotTcpUpstreamHandler(
                    tcpProperties, connectionManager, deviceService, messageService, deviceApi, serverId);
            handler.handle(socket);
        });

        // 5. 同步启动服务器，等待结果
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        netServer.listen(result -> {
            if (result.succeeded()) {
                log.info("[startTcpServer][TCP 服务器启动成功] 端口: {}, 服务器ID: {}",
                        result.result().actualPort(), serverId);
            } else {
                log.error("[startTcpServer][TCP 服务器启动失败]", result.cause());
                failure.set(result.cause());
            }
            latch.countDown();
        });

        // 6. 等待启动结果，设置超时
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new RuntimeException("TCP 服务器启动超时");
            }
            if (failure.get() != null) {
                throw new RuntimeException("TCP 服务器启动失败", failure.get());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("TCP 服务器启动被中断", e);
        }
    }

    /**
     * 停止 TCP 服务器
     */
    private void stopTcpServer() {
        if (netServer == null) {
            return;
        }
        log.info("[stopTcpServer][准备关闭 TCP 服务器]");
        CountDownLatch latch = new CountDownLatch(1);
        // 异步关闭，并使用 Latch 等待结果
        netServer.close(result -> {
            if (result.succeeded()) {
                log.info("[stopTcpServer][IoT 网关 TCP 协议处理器已停止]");
            } else {
                log.warn("[stopTcpServer][TCP 服务器关闭失败]", result.cause());
            }
            latch.countDown();
        });

        try {
            // 等待关闭完成，设置超时
            if (!latch.await(10, TimeUnit.SECONDS)) {
                log.warn("[stopTcpServer][关闭 TCP 服务器超时]");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[stopTcpServer][等待 TCP 服务器关闭被中断]", e);
        }
    }
}