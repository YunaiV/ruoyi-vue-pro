package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.handler.downstream;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.AbstractIotProtocolDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.OPC_UA_DOWNSTREAM_NOT_SUPPORTED;

/**
 * IoT OPC UA 下行消息订阅器：只支持属性设置（thing.property.set），交给 {@link IotOpcUaClientDownstreamHandler} 写入
 * <p>
 * biz 已拒绝 OPC UA 产品的 service.invoke、raw.push、配置推送、OTA 等；这里兜底，收到时只记日志
 *
 * @author 芋道源码
 */
@Slf4j
public class IotOpcUaClientDownstreamSubscriber extends AbstractIotProtocolDownstreamSubscriber {

    private final IotOpcUaClientDownstreamHandler downstreamHandler;

    public IotOpcUaClientDownstreamSubscriber(IotProtocol protocol, IotOpcUaClientDownstreamHandler downstreamHandler,
                                              IotMessageBus messageBus) {
        super(protocol, messageBus);
        this.downstreamHandler = downstreamHandler;
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        // 1. 属性设置：写入节点的 Value 属性
        if (IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod().equals(message.getMethod())
                && !IotDeviceMessageUtils.isReplyMessage(message)) {
            downstreamHandler.handle(message);
            return;
        }
        // 2. 其它指令（服务调用等）：不支持
        // TODO【需求】OPC UA 服务调用（thing.service.invoke → Method Call）。背景：需要物模型服务到 ObjectId + MethodId 的映射配置、
        //  输入参数按方法的 InputArguments（DataType、ValueRank）转换、校验 Executable / UserExecutable，结果按 OutputArguments
        //  回复 thing.service.invoke_reply；与属性写入一样默认不开放、显式配置后才允许。
        //  边界：biz 目前拒绝 OPC UA 产品的服务调用，实现时 biz 校验与这里需要一起放开；透传、配置推送、OTA、拓扑变更不在范围内
        if (IotDeviceMessageUtils.isCommandRequest(message)) {
            log.warn("[handleMessage][设备 {} 的下行消息 {}({}) 被忽略: {}]", message.getDeviceId(),
                    message.getMethod(), message.getId(), OPC_UA_DOWNSTREAM_NOT_SUPPORTED.getMsg());
            return;
        }
        log.debug("[handleMessage][设备 {} 的下行消息 {}({}) 无需处理]", message.getDeviceId(),
                message.getMethod(), message.getId());
    }

}
