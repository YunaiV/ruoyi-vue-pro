package cn.iocoder.yudao.module.pms.enums.pm.workitem;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * PMS 工作项优先级枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PmsWorkItemPriorityEnum implements ArrayValuable<Integer> {

    NONE(0, "无"),
    LOW(1, "低"),
    MEDIUM(2, "中"),
    HIGH(3, "高");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(PmsWorkItemPriorityEnum::getPriority).toArray(Integer[]::new);

    /**
     * 优先级
     */
    private final Integer priority;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static PmsWorkItemPriorityEnum valueOf(Integer priority) {
        return ArrayUtil.firstMatch(item -> item.getPriority().equals(priority), values());
    }

}
