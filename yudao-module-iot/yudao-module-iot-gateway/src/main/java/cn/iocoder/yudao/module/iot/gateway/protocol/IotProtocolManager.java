package cn.iocoder.yudao.module.iot.gateway.protocol;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
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

import java.util.ArrayList;
import java.util.List;

/**
 * IoT 协议管理器：负责根据配置创建和管理协议实例
 *
 * @author 芋道源码
 */
@Slf4j
public class IotProtocolManager implements SmartLifecycle {

    private final IotGatewayProperties gatewayProperties;

    /**
     * 协议实例列表
     */
    private final List<IotProtocol> protocols = new ArrayList<>();

    @Getter
    private volatile boolean running = false;

    public IotProtocolManager(IotGatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
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
            protocol.start();
            protocols.add(protocol);
        }
        running = true;
        log.info("[start][协议管理器启动完成，共启动 {} 个协议实例]", protocols.size());
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        for (IotProtocol protocol : protocols) {
            try {
                protocol.stop();
            } catch (Exception e) {
                log.error("[stop][协议实例 {} 停止失败]", protocol.getId(), e);
            }
        }
        protocols.clear();
        running = false;
        log.info("[stop][协议管理器已停止]");
    }

    /**
     * 创建协议实例
     *
     * @param config 协议实例配置
     * @return 协议实例
     */
    @SuppressWarnings({"EnhancedSwitchMigration"})
    private IotProtocol createProtocol(IotGatewayProperties.ProtocolProperties config) {
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
