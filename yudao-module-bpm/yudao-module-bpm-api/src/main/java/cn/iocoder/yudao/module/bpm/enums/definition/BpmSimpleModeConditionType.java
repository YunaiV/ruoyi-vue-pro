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

    EXPRESSION(1, "条件表达式"),
    RULE(2, "条件规则");

    private final Integer type;
    private final String name;

    public static BpmSimpleModeConditionType valueOf(Integer type) {
        return ArrayUtil.firstMatch(nodeType -> nodeType.getType().equals(type), values());
    }

}
