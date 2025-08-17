package cn.iocoder.yudao.module.iot.service.rule.scene.matcher;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;

/**
 * IoT 场景规则匹配器统一接口
 * <p>
 * 支持触发器匹配和条件匹配两种类型，遵循策略模式设计
 * <p>
 * 匹配器类型说明：
 * - 触发器匹配器：用于匹配主触发条件（如设备消息类型、定时器等）
 * - 条件匹配器：用于匹配子条件（如设备状态、属性值、时间条件等）
 *
 * @author HUIHUI
 */
public interface IotSceneRuleMatcher {

    /**
     * 匹配器类型枚举
     */
    enum MatcherTypeEnum {

        /**
         * 触发器匹配器 - 用于匹配主触发条件
         */
        TRIGGER,

        /**
         * 条件匹配器 - 用于匹配子条件
         */
        CONDITION

    }

    /**
     * 获取匹配器类型
     *
     * @return 匹配器类型
     */
    MatcherTypeEnum getMatcherType();

    // TODO @puhui999：【重要】有个思路，IotSceneRuleMatcher 拆分成 2 种 mather 接口；然后 AbstractIotSceneRuleMatcher 是个 Helper 工具类；

    // TODO @puhui999：是不是和 AbstractSceneRuleMatcher 一样，分下块；

    /**
     * 获取支持的触发器类型（仅触发器匹配器需要实现）
     *
     * @return 触发器类型枚举，条件匹配器返回 null
     */
    default IotSceneRuleTriggerTypeEnum getSupportedTriggerType() {
        return null;
    }

    /**
     * 获取支持的条件类型（仅条件匹配器需要实现）
     *
     * @return 条件类型枚举，触发器匹配器返回 null
     */
    default IotSceneRuleConditionTypeEnum getSupportedConditionType() {
        return null;
    }

    /**
     * 检查触发器是否匹配消息（仅触发器匹配器需要实现）
     *
     * @param message 设备消息
     * @param trigger 触发器配置
     * @return 是否匹配
     */
    default boolean isMatched(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger) {
        throw new UnsupportedOperationException("触发器匹配方法仅支持触发器匹配器");
    }

    /**
     * 检查条件是否匹配消息（仅条件匹配器需要实现）
     *
     * @param message   设备消息
     * @param condition 触发条件
     * @return 是否匹配
     */
    default boolean isMatched(IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition) {
        throw new UnsupportedOperationException("条件匹配方法仅支持条件匹配器");
    }

    /**
     * 获取匹配优先级（数值越小优先级越高）
     * <p>
     * 用于在多个匹配器支持同一类型时确定优先级
     *
     * @return 优先级数值
     */
    default int getPriority() {
        return 100;
    }

    // TODO @puhui999：如果目前没自定义，体感可以删除哈；
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
