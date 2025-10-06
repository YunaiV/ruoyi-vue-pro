package cn.iocoder.yudao.module.iot.service.rule.scene.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertRecordDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleActionTypeEnum;
import cn.iocoder.yudao.module.iot.service.alert.IotAlertRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * IoT 告警恢复的 {@link IotSceneRuleAction} 实现类
 *
 * @author 芋道源码
 */
@Component
public class IotAlertRecoverSceneRuleAction implements IotSceneRuleAction {

    private static final String PROCESS_REMARK = "告警自动回复，基于【{}】场景联动规则";

    @Resource
    private IotAlertRecordService alertRecordService;

    @Override
    public void execute(IotDeviceMessage message,
                        IotSceneRuleDO rule, IotSceneRuleDO.Action actionConfig) throws Exception {
        Long deviceId = message != null ? message.getDeviceId() : null;
        List<IotAlertRecordDO> alertRecords = alertRecordService.getAlertRecordListBySceneRuleId(
                rule.getId(), deviceId, false);
        if (CollUtil.isEmpty(alertRecords)) {
            return;
        }
        alertRecordService.processAlertRecordList(convertList(alertRecords, IotAlertRecordDO::getId),
                StrUtil.format(PROCESS_REMARK, rule.getName()));
    }

    @Override
    public IotSceneRuleActionTypeEnum getType() {
        return IotSceneRuleActionTypeEnum.ALERT_RECOVER;
    }

}
