package cn.iocoder.yudao.module.iot.service.rule.scene.matcher;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;

/**
 * IoT 场景规则触发器匹配策略接口
 * <p>
 * 用于实现不同类型触发器的匹配逻辑，遵循策略模式设计
 *
 * @author HUIHUI
 */
public interface IotSceneRuleTriggerMatcher {

    /**
     * 获取支持的触发器类型
     *
     * @return 触发器类型枚举
     */
    IotSceneRuleTriggerTypeEnum getSupportedTriggerType();

    /**
     * 检查触发器是否匹配消息
     *
     * @param message 设备消息
     * @param trigger 触发器配置
     * @return 是否匹配
     */
    boolean isMatched(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger);

    /**
     * 获取匹配优先级（数值越小优先级越高）
     * <p>
     * 用于在多个匹配器支持同一触发器类型时确定优先级
     *
     * @return 优先级数值
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 获取匹配器名称，用于日志和调试
     *
     * @return 匹配器名称
     */
    default String getMatcherName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 是否启用该匹配器
     * <p>
     * 可用于动态开关某些匹配器
     *
     * @return 是否启用
     */
    default boolean isEnabled() {
        return true;
    }
}
