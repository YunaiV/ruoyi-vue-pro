package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.handler.downstream;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.AbstractIotProtocolDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.OPC_UA_DOWNSTREAM_NOT_SUPPORTED;

/**
 * IoT OPC UA 下行消息订阅器：一期只读采集，不支持写入、服务调用和透传
 * <p>
 * biz 已拒绝 OPC UA 产品的 property.set、service.invoke 和 raw.push；这里兜底，收到时只记日志
 *
 * @author 芋道源码
 */
@Slf4j
public class IotOpcUaClientDownstreamSubscriber extends AbstractIotProtocolDownstreamSubscriber {

    public IotOpcUaClientDownstreamSubscriber(IotProtocol protocol, IotMessageBus messageBus) {
        super(protocol, messageBus);
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        if (IotDeviceMessageUtils.isCommandRequest(message)) {
            log.warn("[handleMessage][设备 {} 的下行消息 {}({}) 被忽略: {}]", message.getDeviceId(),
                    message.getMethod(), message.getId(), OPC_UA_DOWNSTREAM_NOT_SUPPORTED.getMsg());
            return;
        }
        log.debug("[handleMessage][设备 {} 的下行消息 {}({}) 无需处理]", message.getDeviceId(),
                message.getMethod(), message.getId());
    }

}
