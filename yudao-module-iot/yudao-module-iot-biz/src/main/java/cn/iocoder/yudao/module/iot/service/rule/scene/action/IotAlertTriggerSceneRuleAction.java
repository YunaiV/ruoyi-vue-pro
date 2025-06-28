package cn.iocoder.yudao.module.iot.service.rule.scene.action;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotRuleSceneDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneActionTypeEnum;
import cn.iocoder.yudao.module.iot.service.alert.IotAlertConfigService;
import cn.iocoder.yudao.module.iot.service.alert.IotAlertRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

// TODO @puhui999、@芋艿：未测试；需要场景联动开发完
/**
 * IoT 告警触发的 {@link IotSceneRuleAction} 实现类
 *
 * @author 芋道源码
 */
@Component
public class IotAlertTriggerSceneRuleAction implements IotSceneRuleAction {

    @Resource
    private IotAlertConfigService alertConfigService;

    @Resource
    private IotAlertRecordService alertRecordService;

    @Override
    public void execute(@Nullable IotDeviceMessage message,
                        IotRuleSceneDO rule, IotRuleSceneDO.ActionConfig actionConfig) throws Exception {
        List<IotAlertConfigDO> alertConfigs = alertConfigService.getAlertConfigListBySceneRuleIdAndStatus(
                rule.getId(), CommonStatusEnum.ENABLE.getStatus());
        if (CollUtil.isEmpty(alertConfigs)) {
            return;
        }
        alertConfigs.forEach(alertConfig ->
                alertRecordService.createAlertRecord(alertConfig, message));
    }

    @Override
    public IotRuleSceneActionTypeEnum getType() {
        return IotRuleSceneActionTypeEnum.ALERT_TRIGGER;
    }

}
