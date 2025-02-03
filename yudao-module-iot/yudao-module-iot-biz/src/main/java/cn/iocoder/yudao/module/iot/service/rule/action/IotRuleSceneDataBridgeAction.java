package cn.iocoder.yudao.module.iot.service.rule.action;

import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotRuleSceneDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneActionTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import org.springframework.stereotype.Component;

/**
 * IoT 数据桥梁的 {@link IotRuleSceneAction} 实现类
 *
 * @author 芋道源码
 */
@Component
public class IotRuleSceneDataBridgeAction implements IotRuleSceneAction {

    @Override
    public void execute(IotDeviceMessage message, IotRuleSceneDO.ActionConfig config) {
        // TODO @芋艿：http
        // TODO @芋艿：mq-redis
        // TODO @芋艿：mq-数据库
        // TODO @芋艿：kafka
        // TODO @芋艿：rocketmq
        // TODO @芋艿：rabbitmq
        // TODO @芋艿：mqtt
        // TODO @芋艿：tcp
    }

    @Override
    public IotRuleSceneActionTypeEnum getType() {
        return IotRuleSceneActionTypeEnum.DATA_BRIDGE;
    }

}
