package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 仿钉钉的流程器设计器条件节点的条件类型
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmSimpleModeConditionType {

    CUSTOM_EXPRESSION(1, "自定义条件表达式");

    private final Integer type;
    private final String name;

    public static BpmSimpleModeConditionType valueOf(Integer type) {
        return ArrayUtil.firstMatch(nodeType -> nodeType.getType().equals(type), values());
    }

}
