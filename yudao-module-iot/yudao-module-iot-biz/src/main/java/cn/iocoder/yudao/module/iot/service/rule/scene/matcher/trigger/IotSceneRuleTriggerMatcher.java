package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotSceneRuleMatcher;

/**
 * IoT 场景规则触发器匹配器接口：专门处理主触发条件的匹配逻辑，如设备消息类型、定时器等
 *
 * 触发器匹配器负责判断设备消息是否满足场景规则的主触发条件，是场景规则执行的第一道门槛
 *
 * @author HUIHUI
 */
public interface IotSceneRuleTriggerMatcher extends IotSceneRuleMatcher {

    /**
     * 获取支持的触发器类型
     *
     * @return 触发器类型枚举
     */
    IotSceneRuleTriggerTypeEnum getSupportedTriggerType();

    /**
     * 检查触发器是否匹配消息
     * <p>
     * 判断设备消息是否满足指定的触发器条件
     *
     * @param message 设备消息
     * @param trigger 触发器配置
     * @return 是否匹配
     */
    boolean matches(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger);

}
