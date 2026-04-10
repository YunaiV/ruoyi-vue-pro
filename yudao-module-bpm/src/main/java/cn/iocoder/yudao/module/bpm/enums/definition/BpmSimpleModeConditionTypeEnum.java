package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 仿钉钉的流程器设计器条件节点的条件类型
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmSimpleModeConditionTypeEnum implements ArrayValuable<Integer> {

    EXPRESSION(1, "条件表达式"),
    RULE(2, "条件规则");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BpmSimpleModeConditionTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;

    private final String name;

    public static BpmSimpleModeConditionTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(nodeType -> nodeType.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
