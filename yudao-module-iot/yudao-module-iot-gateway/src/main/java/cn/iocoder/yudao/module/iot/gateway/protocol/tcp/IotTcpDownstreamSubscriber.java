package cn.iocoder.yudao.module.iot.gateway.protocol.tcp;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.IotTcpSessionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router.IotTcpDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * IoT 网关 TCP 下游订阅者：接收下行给设备的消息
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class IotTcpDownstreamSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    private final IotTcpDownstreamHandler downstreamHandler;

    private final IotMessageBus messageBus;

    private final IotTcpUpstreamProtocol protocol;

    private final IotDeviceService deviceService;

    private final IotTcpSessionManager sessionManager;

    public IotTcpDownstreamSubscriber(IotTcpUpstreamProtocol protocol,
            IotDeviceMessageService messageService,
                                      IotDeviceService deviceService,
                                      IotTcpSessionManager sessionManager,
            IotMessageBus messageBus) {
        this.protocol = protocol;
        this.messageBus = messageBus;
        this.deviceService = deviceService;
        this.sessionManager = sessionManager;
        this.downstreamHandler = new IotTcpDownstreamHandler(messageService, deviceService, sessionManager);
    }

    @PostConstruct
    public void init() {
        messageBus.register(this);
        log.info("[init][TCP 下游订阅者初始化完成] 服务器 ID: {}, Topic: {}",
                protocol.getServerId(), getTopic());
    }

    @Override
    public String getTopic() {
        return IotDeviceMessageUtils.buildMessageBusGatewayDeviceMessageTopic(protocol.getServerId());
    }

    @Override
    public String getGroup() {
        // 保证点对点消费，需要保证独立的 Group，所以使用 Topic 作为 Group
        return getTopic();
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        try {
            downstreamHandler.handle(message);
        } catch (Exception e) {
            log.error("[onMessage][处理下行消息失败] 设备 ID: {}, 方法: {}, 消息 ID: {}",
                    message.getDeviceId(), message.getMethod(), message.getId(), e);
        }
    }
}