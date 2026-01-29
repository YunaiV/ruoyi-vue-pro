package cn.iocoder.yudao.module.iot.gateway.protocol.udp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.manager.IotUdpSessionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.router.IotUdpUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

/**
 * IoT 网关 UDP 协议：接收设备上行消息
 * <p>
 * 采用 Vertx DatagramSocket 实现 UDP 服务器，主要功能：
 * 1. 监听 UDP 端口，接收设备消息
 * 2. 定期清理不活跃的设备地址映射
 * 3. 提供 UDP Socket 用于下行消息发送
 *
 * @author 芋道源码
 */
@Slf4j
public class IotUdpUpstreamProtocol {

    private final IotGatewayProperties.UdpProperties udpProperties;

    private final IotDeviceService deviceService;

    private final IotDeviceMessageService messageService;

    private final IotUdpSessionManager sessionManager;

    private final Vertx vertx;

    @Getter
    private final String serverId;

    @Getter
    private DatagramSocket udpSocket;

    /**
     * 会话清理定时器 ID
     */
    private Long cleanTimerId;

    private IotUdpUpstreamHandler upstreamHandler;

    public IotUdpUpstreamProtocol(IotGatewayProperties.UdpProperties udpProperties,
                                  IotDeviceService deviceService,
                                  IotDeviceMessageService messageService,
                                  IotUdpSessionManager sessionManager,
                                  Vertx vertx) {
        this.udpProperties = udpProperties;
        this.deviceService = deviceService;
        this.messageService = messageService;
        this.sessionManager = sessionManager;
        this.vertx = vertx;
        this.serverId = IotDeviceMessageUtils.generateServerId(udpProperties.getPort());
    }

    @PostConstruct
    public void start() {
        // 1. 初始化上行消息处理器
        this.upstreamHandler = new IotUdpUpstreamHandler(this, messageService, deviceService, sessionManager);

        // 2. 创建 UDP Socket 选项
        DatagramSocketOptions options = new DatagramSocketOptions()
                .setReceiveBufferSize(udpProperties.getReceiveBufferSize())
                .setSendBufferSize(udpProperties.getSendBufferSize())
                .setReuseAddress(true);

        // 3. 创建 UDP Socket
        udpSocket = vertx.createDatagramSocket(options);

        // 4. 监听端口
        udpSocket.listen(udpProperties.getPort(), "0.0.0.0", result -> {
            if (result.failed()) {
                log.error("[start][IoT 网关 UDP 协议启动失败]", result.cause());
                return;
            }
            // 设置数据包处理器
            udpSocket.handler(packet -> upstreamHandler.handle(packet, udpSocket));
            log.info("[start][IoT 网关 UDP 协议启动成功，端口：{}，接收缓冲区：{} 字节，发送缓冲区：{} 字节]",
                    udpProperties.getPort(), udpProperties.getReceiveBufferSize(),
                    udpProperties.getSendBufferSize());

            // 5. 启动会话清理定时器
            startSessionCleanTimer();
        });
    }

    @PreDestroy
    public void stop() {
        // 1. 取消会话清理定时器
        if (cleanTimerId != null) {
            vertx.cancelTimer(cleanTimerId);
            cleanTimerId = null;
            log.info("[stop][会话清理定时器已取消]");
        }

        // 2. 关闭 UDP Socket
        if (udpSocket != null) {
            try {
                udpSocket.close().result();
                log.info("[stop][IoT 网关 UDP 协议已停止]");
            } catch (Exception e) {
                log.error("[stop][IoT 网关 UDP 协议停止失败]", e);
            }
        }
    }

    /**
     * 启动会话清理定时器
     */
    private void startSessionCleanTimer() {
        cleanTimerId = vertx.setPeriodic(udpProperties.getSessionCleanIntervalMs(), id -> {
            try {
                // 1. 清理超时的设备地址映射，并获取离线设备列表
                List<Long> offlineDeviceIds = sessionManager.cleanExpiredMappings(udpProperties.getSessionTimeoutMs());

                // 2. 为每个离线设备发送离线消息
                for (Long deviceId : offlineDeviceIds) {
                    sendOfflineMessage(deviceId);
                }
                if (CollUtil.isNotEmpty(offlineDeviceIds)) {
                    log.info("[cleanExpiredMappings][本次清理 {} 个超时设备]", offlineDeviceIds.size());
                }
            } catch (Exception e) {
                log.error("[cleanExpiredMappings][清理超时会话失败]", e);
            }
        });
        log.info("[startSessionCleanTimer][会话清理定时器启动，间隔：{} ms，超时：{} ms]",
                udpProperties.getSessionCleanIntervalMs(), udpProperties.getSessionTimeoutMs());
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
            messageService.sendDeviceMessage(offlineMessage, device.getProductKey(),
                    device.getDeviceName(), serverId);
            log.info("[sendOfflineMessage][发送离线消息，设备 ID: {}，设备名: {}]",
                    deviceId, device.getDeviceName());
        } catch (Exception e) {
            log.error("[sendOfflineMessage][发送离线消息失败，设备 ID: {}]", deviceId, e);
        }
    }

}
