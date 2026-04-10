package cn.iocoder.yudao.module.iot.gateway.protocol;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 协议下行消息订阅者抽象类
 *
 * 负责接收来自消息总线的下行消息，并委托给子类进行业务处理
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Slf4j
public abstract class AbstractIotProtocolDownstreamSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    private final IotProtocol protocol;

    private final IotMessageBus messageBus;

    @Override
    public String getTopic() {
        return IotDeviceMessageUtils.buildMessageBusGatewayDeviceMessageTopic(protocol.getServerId());
    }

    /**
     * 保证点对点消费，需要保证独立的 Group，所以使用 Topic 作为 Group
     */
    @Override
    public String getGroup() {
        return getTopic();
    }

    @Override
    public void start() {
        messageBus.register(this);
        log.info("[start][{} 下行消息订阅成功，Topic：{}]", protocol.getType().name(), getTopic());
    }

    @Override
    public void stop() {
        messageBus.unregister(this);
        log.info("[stop][{} 下行消息订阅已停止，Topic：{}]", protocol.getType().name(), getTopic());
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        log.debug("[onMessage][接收到下行消息, messageId: {}, method: {}, deviceId: {}]",
                message.getId(), message.getMethod(), message.getDeviceId());
        try {
            // 1. 校验
            String method = message.getMethod();
            if (StrUtil.isBlank(method)) {
                log.warn("[onMessage][消息方法为空, messageId: {}, deviceId: {}]",
                        message.getId(), message.getDeviceId());
                return;
            }

            // 2. 处理下行消息
            handleMessage(message);
        } catch (Exception e) {
            log.error("[onMessage][处理下行消息失败, messageId: {}, method: {}, deviceId: {}]",
                    message.getId(), message.getMethod(), message.getDeviceId(), e);
        }
    }

    /**
     * 处理下行消息
     *
     * @param message 下行消息
     */
    protected abstract void handleMessage(IotDeviceMessage message);

}
