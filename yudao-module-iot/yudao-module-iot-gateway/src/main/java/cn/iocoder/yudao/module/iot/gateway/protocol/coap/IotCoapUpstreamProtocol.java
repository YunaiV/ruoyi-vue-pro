package cn.iocoder.yudao.module.iot.gateway.protocol.coap;

import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.router.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.elements.config.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * IoT 网关 CoAP 协议：接收设备上行消息
 *
 * 基于 Eclipse Californium 实现，支持：
 * 1. 认证：POST /auth
 * 2. 属性上报：POST /topic/sys/{productKey}/{deviceName}/thing/property/post
 * 3. 事件上报：POST /topic/sys/{productKey}/{deviceName}/thing/event/post
 *
 * @author 芋道源码
 */
@Slf4j
public class IotCoapUpstreamProtocol {

    private final IotGatewayProperties.CoapProperties coapProperties;

    private CoapServer coapServer;

    @Getter
    private final String serverId;

    public IotCoapUpstreamProtocol(IotGatewayProperties.CoapProperties coapProperties) {
        this.coapProperties = coapProperties;
        this.serverId = IotDeviceMessageUtils.generateServerId(coapProperties.getPort());
    }

    @PostConstruct
    public void start() {
        try {
            // 1.1 创建网络配置（Californium 3.x API）
            Configuration config = Configuration.createStandardWithoutFile();
            config.set(CoapConfig.COAP_PORT, coapProperties.getPort());
            config.set(CoapConfig.MAX_MESSAGE_SIZE, coapProperties.getMaxMessageSize());
            config.set(CoapConfig.ACK_TIMEOUT, coapProperties.getAckTimeout(), TimeUnit.MILLISECONDS);
            config.set(CoapConfig.MAX_RETRANSMIT, coapProperties.getMaxRetransmit());
            // 1.2 创建 CoAP 服务器
            coapServer = new CoapServer(config);

            // 2.1 添加 /auth 认证资源
            IotCoapAuthHandler authHandler = new IotCoapAuthHandler();
            IotCoapAuthResource authResource = new IotCoapAuthResource(this, authHandler);
            coapServer.add(authResource);
            // 2.2 添加 /auth/register/device 设备动态注册资源（一型一密）
            IotCoapRegisterHandler registerHandler = new IotCoapRegisterHandler();
            IotCoapRegisterResource registerResource = new IotCoapRegisterResource(registerHandler);
            authResource.add(new CoapResource("register") {{
                add(registerResource);
            }});
            // 2.3 添加 /topic 根资源（用于上行消息）
            IotCoapUpstreamHandler upstreamHandler = new IotCoapUpstreamHandler();
            IotCoapUpstreamTopicResource topicResource = new IotCoapUpstreamTopicResource(this, upstreamHandler);
            coapServer.add(topicResource);

            // 3. 启动服务器
            coapServer.start();
            log.info("[start][IoT 网关 CoAP 协议启动成功，端口：{}，资源：/auth, /auth/register/device, /topic]", coapProperties.getPort());
        } catch (Exception e) {
            log.error("[start][IoT 网关 CoAP 协议启动失败]", e);
            throw e;
        }
    }

    @PreDestroy
    public void stop() {
        if (coapServer != null) {
            try {
                coapServer.stop();
                log.info("[stop][IoT 网关 CoAP 协议已停止]");
            } catch (Exception e) {
                log.error("[stop][IoT 网关 CoAP 协议停止失败]", e);
            }
        }
    }

}
