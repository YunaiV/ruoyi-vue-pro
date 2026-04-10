package cn.iocoder.yudao.module.iot.gateway.protocol.coap;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.downstream.IotCoapDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream.IotCoapAuthHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream.IotCoapAuthResource;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream.IotCoapRegisterHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream.IotCoapRegisterResource;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream.IotCoapRegisterSubHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream.IotCoapRegisterSubResource;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream.IotCoapUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream.IotCoapUpstreamTopicResource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.elements.config.Configuration;
import cn.hutool.core.lang.Assert;

import java.util.concurrent.TimeUnit;

/**
 * IoT CoAP 协议实现
 * <p>
 * 基于 Eclipse Californium 实现，支持：
 * 1. 认证：POST /auth
 * 2. 设备动态注册：POST /auth/register/device
 * 3. 子设备动态注册：POST /auth/register/sub-device/{productKey}/{deviceName}
 * 4. 属性上报：POST /topic/sys/{productKey}/{deviceName}/thing/property/post
 * 5. 事件上报：POST /topic/sys/{productKey}/{deviceName}/thing/event/post
 *
 * @author 芋道源码
 */
@Slf4j
public class IotCoapProtocol implements IotProtocol {

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
     * CoAP 服务器
     */
    private CoapServer coapServer;

    /**
     * 下行消息订阅者
     */
    private IotCoapDownstreamSubscriber downstreamSubscriber;

    public IotCoapProtocol(ProtocolProperties properties) {
        IotCoapConfig coapConfig = properties.getCoap();
        Assert.notNull(coapConfig, "CoAP 协议配置（coap）不能为空");
        this.properties = properties;
        this.serverId = IotDeviceMessageUtils.generateServerId(properties.getPort());
    }

    @Override
    public String getId() {
        return properties.getId();
    }

    @Override
    public IotProtocolTypeEnum getType() {
        return IotProtocolTypeEnum.COAP;
    }

    @Override
    public void start() {
        if (running) {
            log.warn("[start][IoT CoAP 协议 {} 已经在运行中]", getId());
            return;
        }

        try {
            // 1.1 创建 CoAP 配置
            IotCoapConfig coapConfig = properties.getCoap();
            Configuration config = Configuration.createStandardWithoutFile();
            config.set(CoapConfig.COAP_PORT, properties.getPort());
            config.set(CoapConfig.MAX_MESSAGE_SIZE, coapConfig.getMaxMessageSize());
            config.set(CoapConfig.ACK_TIMEOUT, coapConfig.getAckTimeoutMs(), TimeUnit.MILLISECONDS);
            config.set(CoapConfig.MAX_RETRANSMIT, coapConfig.getMaxRetransmit());
            // 1.2 创建 CoAP 服务器
            coapServer = new CoapServer(config);

            // 2.1 添加 /auth 认证资源
            IotCoapAuthHandler authHandler = new IotCoapAuthHandler(serverId);
            IotCoapAuthResource authResource = new IotCoapAuthResource(authHandler);
            coapServer.add(authResource);
            // 2.2 添加 /auth/register/device 设备动态注册资源（一型一密）
            IotCoapRegisterHandler registerHandler = new IotCoapRegisterHandler();
            IotCoapRegisterResource registerResource = new IotCoapRegisterResource(registerHandler);
            // 2.3 添加 /auth/register/sub-device/{productKey}/{deviceName} 子设备动态注册资源
            IotCoapRegisterSubHandler registerSubHandler = new IotCoapRegisterSubHandler();
            IotCoapRegisterSubResource registerSubResource = new IotCoapRegisterSubResource(registerSubHandler);
            authResource.add(new CoapResource("register") {{
                add(registerResource);
                add(registerSubResource);
            }});
            // 2.4 添加 /topic 根资源（用于上行消息）
            IotCoapUpstreamHandler upstreamHandler = new IotCoapUpstreamHandler(serverId);
            IotCoapUpstreamTopicResource topicResource = new IotCoapUpstreamTopicResource(serverId, upstreamHandler);
            coapServer.add(topicResource);

            // 3. 启动服务器
            coapServer.start();
            running = true;
            log.info("[start][IoT CoAP 协议 {} 启动成功，端口：{}，serverId：{}]",
                    getId(), properties.getPort(), serverId);

            // 4. 启动下行消息订阅者
            IotMessageBus messageBus = SpringUtil.getBean(IotMessageBus.class);
            this.downstreamSubscriber = new IotCoapDownstreamSubscriber(this, messageBus);
            this.downstreamSubscriber.start();
        } catch (Exception e) {
            log.error("[start][IoT CoAP 协议 {} 启动失败]", getId(), e);
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
                log.info("[stop][IoT CoAP 协议 {} 下行消息订阅者已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT CoAP 协议 {} 下行消息订阅者停止失败]", getId(), e);
            }
            downstreamSubscriber = null;
        }

        // 2. 关闭 CoAP 服务器
        if (coapServer != null) {
            try {
                coapServer.stop();
                coapServer.destroy();
                coapServer = null;
                log.info("[stop][IoT CoAP 协议 {} 服务器已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT CoAP 协议 {} 服务器停止失败]", getId(), e);
            }
        }
        running = false;
        log.info("[stop][IoT CoAP 协议 {} 已停止]", getId());
    }

}
