package cn.iocoder.yudao.module.iot.gateway.protocol;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.BooleanUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotGatewayServerAliveReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.IotCoapProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.IotModbusTcpClientProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.IotModbusTcpServerProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.IotUdpProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.IotWebSocketProtocol;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * IoT 协议管理器：负责根据配置创建和管理协议实例
 *
 * @author 芋道源码
 */
@Slf4j
public class IotProtocolManager implements SmartLifecycle {

    /**
     * 存活标记的续期间隔（秒）
     *
     * 存活标记由 biz 写入自己的 Redis，过期时间见 DeviceServerIdRedisDAO
     */
    static final long ALIVE_INTERVAL_SECONDS = 20;

    private final IotGatewayProperties gatewayProperties;

    private final IotDeviceCommonApi deviceApi;

    /**
     * 协议实例列表
     *
     * 使用 CopyOnWriteArrayList 的原因：续期线程会并发遍历
     */
    private final List<IotProtocol> protocols = new CopyOnWriteArrayList<>();

    /**
     * 协议实例的启动时间（毫秒）
     *
     * 每次 start 成功都记录：biz 的离线任务据此判断，设备上线之后，协议实例是否重启过
     */
    private final Map<IotProtocol, Long> startTimes = new ConcurrentHashMap<>();

    /**
     * 存活标记的续期线程
     */
    private ScheduledExecutorService aliveExecutor;

    @Getter
    private volatile boolean running = false;

    public IotProtocolManager(IotGatewayProperties gatewayProperties, IotDeviceCommonApi deviceApi) {
        this.gatewayProperties = gatewayProperties;
        this.deviceApi = deviceApi;
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        List<IotGatewayProperties.ProtocolProperties> protocolConfigs = gatewayProperties.getProtocols();
        if (CollUtil.isEmpty(protocolConfigs)) {
            log.info("[start][没有配置协议实例，跳过启动]");
            return;
        }

        for (IotGatewayProperties.ProtocolProperties config : protocolConfigs) {
            if (BooleanUtil.isFalse(config.getEnabled())) {
                log.info("[start][协议实例 {} 未启用，跳过]", config.getId());
                continue;
            }
            IotProtocol protocol = createProtocol(config);
            if (protocol == null) {
                continue;
            }
            // 启动前取时间：保证启动后才连上的设备，上线时间都不早于它
            long startTime = System.currentTimeMillis();
            protocol.start();
            startTimes.put(protocol, startTime);
            protocols.add(protocol);
        }
        // 定时续期存活标记
        aliveExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("iot-gateway-alive-", true));
        aliveExecutor.scheduleWithFixedDelay(this::writeAlive, 0, ALIVE_INTERVAL_SECONDS, TimeUnit.SECONDS);
        running = true;
        log.info("[start][协议管理器启动完成，共启动 {} 个协议实例]", protocols.size());
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        // 先停止续期，再停止协议：停止后存活标记不再续期，过期后 biz 的离线任务即可判定
        // 注意：已经发出的上报不会被撤销，biz 仍可能稍后写入，存活标记会相应晚一些过期
        stopAliveExecutor();
        for (IotProtocol protocol : protocols) {
            try {
                protocol.stop();
            } catch (Exception e) {
                log.error("[stop][协议实例 {} 停止失败]", protocol.getId(), e);
            }
        }
        protocols.clear();
        startTimes.clear();
        running = false;
        log.info("[stop][协议管理器已停止]");
    }

    // DONE @AI：方法注释；（已补充）
    /**
     * 停止存活标记的续期线程：最多等待 5 秒
     *
     * 只停止线程，不撤销已经发出的上报
     */
    private void stopAliveExecutor() {
        if (aliveExecutor == null) {
            return;
        }
        aliveExecutor.shutdownNow();
        try {
            if (!aliveExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("[stopAliveExecutor][存活标记续期线程未能在 5 秒内退出]");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        aliveExecutor = null;
    }

    /**
     * 续期各协议实例的存活标记：一轮一次批量上报给 biz，值为协议实例的启动时间
     *
     * EMQX 的连接由 EMQX 维护，不上报存活标记
     */
    void writeAlive() {
        // 任何异常都不抛出：否则周期任务会永久停止。远程调用失败返回 error；同 JVM 直接调用时，可能直接抛出异常
        try {
            // 1. 收集运行中协议实例的启动时间：serverId -> startTime
            Map<String, Long> serverStartTimes = new HashMap<>();
            for (IotProtocol protocol : protocols) {
                if (!protocol.isRunning() || protocol.getType() == IotProtocolTypeEnum.EMQX) {
                    continue;
                }
                Long startTime = this.startTimes.get(protocol);
                if (startTime != null) {
                    serverStartTimes.put(protocol.getServerId(), startTime);
                }
            }
            if (serverStartTimes.isEmpty()) {
                return;
            }

            // 2. 批量上报：部分失败时 data 为 false，下一轮继续续期
            CommonResult<Boolean> result = deviceApi.keepAliveGatewayServers(
                    new IotGatewayServerAliveReqDTO().setStartTimes(serverStartTimes));
            if (result == null || !result.isSuccess() || !Boolean.TRUE.equals(result.getData())) {
                log.warn("[writeAlive][续期存活标记未全部成功，serverStartTimes({}) result({})]", serverStartTimes, result);
            }
        } catch (Throwable e) {
            log.warn("[writeAlive][续期存活标记异常]", e);
        }
    }

    /**
     * 创建协议实例
     *
     * @param config 协议实例配置
     * @return 协议实例
     */
    @SuppressWarnings({"EnhancedSwitchMigration"})
    IotProtocol createProtocol(IotGatewayProperties.ProtocolProperties config) {
        IotProtocolTypeEnum protocolType = IotProtocolTypeEnum.of(config.getProtocol());
        if (protocolType == null) {
            log.error("[createProtocol][协议实例 {} 的协议类型 {} 不存在]", config.getId(), config.getProtocol());
            return null;
        }
        switch (protocolType) {
            case HTTP:
                return createHttpProtocol(config);
            case TCP:
                return createTcpProtocol(config);
            case UDP:
                return createUdpProtocol(config);
            case COAP:
                return createCoapProtocol(config);
            case WEBSOCKET:
                return createWebSocketProtocol(config);
            case MQTT:
                return createMqttProtocol(config);
            case EMQX:
                return createEmqxProtocol(config);
            case MODBUS_TCP_CLIENT:
                return createModbusTcpClientProtocol(config);
            case MODBUS_TCP_SERVER:
                return createModbusTcpServerProtocol(config);
            default:
                throw new IllegalArgumentException(String.format(
                        "[createProtocol][协议实例 %s 的协议类型 %s 暂不支持]", config.getId(), protocolType));
        }
    }

    /**
     * 创建 HTTP 协议实例
     *
     * @param config 协议实例配置
     * @return HTTP 协议实例
     */
    private IotHttpProtocol createHttpProtocol(IotGatewayProperties.ProtocolProperties config) {
        return new IotHttpProtocol(config);
    }

    /**
     * 创建 TCP 协议实例
     *
     * @param config 协议实例配置
     * @return TCP 协议实例
     */
    private IotTcpProtocol createTcpProtocol(IotGatewayProperties.ProtocolProperties config) {
        return new IotTcpProtocol(config);
    }

    /**
     * 创建 UDP 协议实例
     *
     * @param config 协议实例配置
     * @return UDP 协议实例
     */
    private IotUdpProtocol createUdpProtocol(IotGatewayProperties.ProtocolProperties config) {
        return new IotUdpProtocol(config);
    }

    /**
     * 创建 CoAP 协议实例
     *
     * @param config 协议实例配置
     * @return CoAP 协议实例
     */
    private IotCoapProtocol createCoapProtocol(IotGatewayProperties.ProtocolProperties config) {
        return new IotCoapProtocol(config);
    }

    /**
     * 创建 WebSocket 协议实例
     *
     * @param config 协议实例配置
     * @return WebSocket 协议实例
     */
    private IotWebSocketProtocol createWebSocketProtocol(IotGatewayProperties.ProtocolProperties config) {
        return new IotWebSocketProtocol(config);
    }

    /**
     * 创建 MQTT 协议实例
     *
     * @param config 协议实例配置
     * @return MQTT 协议实例
     */
    private IotMqttProtocol createMqttProtocol(IotGatewayProperties.ProtocolProperties config) {
        return new IotMqttProtocol(config);
    }

    /**
     * 创建 EMQX 协议实例
     *
     * @param config 协议实例配置
     * @return EMQX 协议实例
     */
    private IotEmqxProtocol createEmqxProtocol(IotGatewayProperties.ProtocolProperties config) {
        return new IotEmqxProtocol(config);
    }

    /**
     * 创建 Modbus TCP Client 协议实例
     *
     * @param config 协议实例配置
     * @return Modbus TCP Client 协议实例
     */
    private IotModbusTcpClientProtocol createModbusTcpClientProtocol(IotGatewayProperties.ProtocolProperties config) {
        return new IotModbusTcpClientProtocol(config);
    }

    /**
     * 创建 Modbus TCP Server 协议实例
     *
     * @param config 协议实例配置
     * @return Modbus TCP Server 协议实例
     */
    private IotModbusTcpServerProtocol createModbusTcpServerProtocol(IotGatewayProperties.ProtocolProperties config) {
        return new IotModbusTcpServerProtocol(config);
    }

}
