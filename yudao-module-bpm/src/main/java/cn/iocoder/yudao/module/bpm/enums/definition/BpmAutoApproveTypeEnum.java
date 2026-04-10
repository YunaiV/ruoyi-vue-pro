package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * BPM 自动去重的类型的枚举
 *
 * @author Lesan
 */
@Getter
@AllArgsConstructor
public enum BpmAutoApproveTypeEnum implements ArrayValuable<Integer> {

    NONE(0, "不自动通过"),
    APPROVE_ALL(1, "仅审批一次，后续重复的审批节点均自动通过"),
    APPROVE_SEQUENT(2, "仅针对连续审批的节点自动通过");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BpmAutoApproveTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}