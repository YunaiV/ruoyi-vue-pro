package cn.iocoder.yudao.module.pms.enums.pm.workitem;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * PMS 工作项类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PmsWorkItemTypeEnum implements ArrayValuable<Integer> {

    REQUIREMENT(2, "需求"),
    TASK(3, "任务"),
    DEFECT(4, "缺陷");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(PmsWorkItemTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static PmsWorkItemTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
