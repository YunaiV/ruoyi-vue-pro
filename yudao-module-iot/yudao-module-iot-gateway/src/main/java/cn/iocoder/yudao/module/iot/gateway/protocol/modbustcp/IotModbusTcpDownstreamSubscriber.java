package cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp.router.IotModbusTcpDownstreamHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT Modbus TCP 下行消息订阅器：订阅消息总线的下行消息并转发给处理器
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotModbusTcpDownstreamSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    private final IotModbusTcpUpstreamProtocol upstreamProtocol;
    private final IotModbusTcpDownstreamHandler downstreamHandler;
    private final IotMessageBus messageBus;

    @PostConstruct
    public void subscribe() {
        messageBus.register(this);
        log.info("[subscribe][Modbus TCP 下行消息订阅器已启动, topic={}]", getTopic());
    }

    @Override
    public String getTopic() {
        return IotDeviceMessageUtils.buildMessageBusGatewayDeviceMessageTopic(upstreamProtocol.getServerId());
    }

    @Override
    public String getGroup() {
        return getTopic(); // 点对点消费
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        log.debug("[onMessage][收到下行消息: {}]", message);
        try {
            downstreamHandler.handle(message);
        } catch (Exception e) {
            log.error("[onMessage][处理下行消息失败]", e);
        }
    }

}
