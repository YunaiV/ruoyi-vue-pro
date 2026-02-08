package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster.handler.downstream;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.AbstractIotProtocolDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster.IotModbusTcpMasterProtocol;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT Modbus TCP 下行消息订阅器：订阅消息总线的下行消息并转发给处理器
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpMasterDownstreamSubscriber extends AbstractIotProtocolDownstreamSubscriber {

    private final IotModbusTcpMasterDownstreamHandler downstreamHandler;

    public IotModbusTcpMasterDownstreamSubscriber(IotModbusTcpMasterProtocol protocol,
                                                  IotModbusTcpMasterDownstreamHandler downstreamHandler,
                                                  IotMessageBus messageBus) {
        super(protocol, messageBus);
        this.downstreamHandler = downstreamHandler;
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        downstreamHandler.handle(message);
    }

}
