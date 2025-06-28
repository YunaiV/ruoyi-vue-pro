package cn.iocoder.yudao.module.iot.service.rule.scene.action;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotRuleSceneDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneActionTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * IoT 告警触发的 {@link IotSceneRuleAction} 实现类
 *
 * @author 芋道源码
 */
@Component
public class IotAlertTriggerRuleSceneAction implements IotSceneRuleAction {

    @Override
    public void execute(@Nullable IotDeviceMessage message, IotRuleSceneDO.ActionConfig config) {
        // TODO @AI：
    }

    @Override
    public IotRuleSceneActionTypeEnum getType() {
        return IotRuleSceneActionTypeEnum.ALERT_TRIGGER;
    }

}
