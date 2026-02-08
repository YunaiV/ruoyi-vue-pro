package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.handler.downstream;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocolDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.IotModbusTcpSlaveProtocol;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT Modbus TCP Slave 下行消息订阅器：订阅消息总线的下行消息并转发给处理器
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpSlaveDownstreamSubscriber extends IotProtocolDownstreamSubscriber {

    private final IotModbusTcpSlaveDownstreamHandler downstreamHandler;

    public IotModbusTcpSlaveDownstreamSubscriber(IotModbusTcpSlaveProtocol protocol,
                                                  IotModbusTcpSlaveDownstreamHandler downstreamHandler,
                                                  IotMessageBus messageBus) {
        super(protocol, messageBus);
        this.downstreamHandler = downstreamHandler;
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        downstreamHandler.handle(message);
    }

}
