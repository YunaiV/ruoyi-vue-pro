package cn.iocoder.yudao.module.iot.mq.consumer.rule;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.service.rule.scene.IotSceneRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

// TODO @puhui999：后面重构哈
/**
 * 针对 {@link IotDeviceMessage} 的消费者，处理规则场景
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class IotSceneRuleMessageHandler implements IotMessageSubscriber<IotDeviceMessage> {

    @Resource
    private IotSceneRuleService sceneRuleService;

    @Resource
    private IotMessageBus messageBus;

    @PostConstruct
    public void init() {
        messageBus.register(this);
    }

    @Override
    public String getTopic() {
        return IotDeviceMessage.MESSAGE_BUS_DEVICE_MESSAGE_TOPIC;
    }

    @Override
    public String getGroup() {
        return "iot_rule_consumer";
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        sceneRuleService.executeSceneRuleByDevice(message);
    }

}
