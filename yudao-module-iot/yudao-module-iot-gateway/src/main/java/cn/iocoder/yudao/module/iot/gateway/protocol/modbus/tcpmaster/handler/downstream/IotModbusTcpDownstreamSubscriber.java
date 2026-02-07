package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster.handler.downstream;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster.IotModbusTcpMasterProtocol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// TODO @AI：是不是可以继承 /Users/yunai/Java/ruoyi-vue-pro-jdk25/yudao-module-iot/yudao-module-iot-gateway/src/main/java/cn/iocoder/yudao/module/iot/gateway/protocol/IotProtocolDownstreamSubscriber.java
/**
 * IoT Modbus TCP 下行消息订阅器：订阅消息总线的下行消息并转发给处理器
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotModbusTcpDownstreamSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    private final IotModbusTcpMasterProtocol protocol;
    private final IotModbusTcpDownstreamHandler downstreamHandler;
    private final IotMessageBus messageBus;

    /**
     * 启动订阅
     */
    public void start() {
        messageBus.register(this);
        log.info("[start][Modbus TCP Master 下行消息订阅器已启动, topic={}]", getTopic());
    }

    /**
     * 停止订阅
     */
    public void stop() {
        messageBus.unregister(this);
        log.info("[stop][Modbus TCP Master 下行消息订阅器已停止]");
    }

    @Override
    public String getTopic() {
        return IotDeviceMessageUtils.buildMessageBusGatewayDeviceMessageTopic(protocol.getServerId());
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
