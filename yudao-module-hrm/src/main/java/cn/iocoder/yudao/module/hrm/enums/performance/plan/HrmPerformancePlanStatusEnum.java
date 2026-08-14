package cn.iocoder.yudao.module.hrm.enums.performance.plan;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 绩效计划状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmPerformancePlanStatusEnum implements ArrayValuable<Integer> {

    DRAFT(1, "草稿"),
    NOT_STARTED(2, "未开始"),
    RUNNING(3, "进行中"),
    ARCHIVED(4, "已归档"),
    TERMINATED(5, "已终止");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmPerformancePlanStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmPerformancePlanStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}
