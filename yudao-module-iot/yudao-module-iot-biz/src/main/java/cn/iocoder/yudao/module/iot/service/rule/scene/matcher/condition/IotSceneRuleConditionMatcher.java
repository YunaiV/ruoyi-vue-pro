package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.condition;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotSceneRuleMatcher;

/**
 * IoT 场景规则条件匹配器接口：专门处理子条件的匹配逻辑，如设备状态、属性值、时间条件等
 *
 * 条件匹配器负责判断设备消息是否满足场景规则的附加条件，在触发器匹配成功后进行进一步的条件筛选
 *
 * @author HUIHUI
 */
public interface IotSceneRuleConditionMatcher extends IotSceneRuleMatcher {

    /**
     * 获取支持的条件类型
     *
     * @return 条件类型枚举
     */
    IotSceneRuleConditionTypeEnum getSupportedConditionType();

    /**
     * 检查条件是否匹配消息
     * <p>
     * 判断设备消息是否满足指定的触发条件
     *
     * @param message   设备消息
     * @param condition 触发条件
     * @return 是否匹配
     */
    boolean matches(IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition);

}
