package cn.iocoder.yudao.module.iot.enums.rule;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IoT 场景规则条件层级枚举
 * <p>
 * 用于区分主条件（触发器级别）和子条件（条件分组级别）
 *
 * @author HUIHUI
 */
@AllArgsConstructor
@Getter
public enum IotSceneRuleConditionLevelEnum {

    /**
     * 主条件 - 触发器级别的条件
     * 用于判断触发器本身是否匹配（如消息类型、设备标识等）
     */
    PRIMARY(1, "主条件"),

    /**
     * 子条件 - 条件分组级别的条件
     * 用于判断具体的业务条件（如设备状态、属性值、时间条件等）
     */
    SECONDARY(2, "子条件");

    /**
     * 条件层级
     */
    private final Integer level;

    /**
     * 条件层级名称
     */
    private final String name;

    /**
     * 根据层级值获取枚举
     *
     * @param level 层级值
     * @return 条件层级枚举
     */
    public static IotSceneRuleConditionLevelEnum levelOf(Integer level) {
        if (level == null) {
            return null;
        }
        for (IotSceneRuleConditionLevelEnum levelEnum : values()) {
            if (levelEnum.getLevel().equals(level)) {
                return levelEnum;
            }
        }
        return null;
    }

    /**
     * 判断是否为主条件
     *
     * @return 是否为主条件
     */
    public boolean isPrimary() {
        return this == PRIMARY;
    }

    /**
     * 判断是否为子条件
     *
     * @return 是否为子条件
     */
    public boolean isSecondary() {
        return this == SECONDARY;
    }
}
