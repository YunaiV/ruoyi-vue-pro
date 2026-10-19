package cn.iocoder.yudao.module.pms.enums.pm.workitem;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * PMS 工作项生命周期状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PmsWorkItemLifecycleStatusEnum implements ArrayValuable<Integer> {

    ACTIVE(1, "正常"),
    ARCHIVED(2, "已归档"),
    RECYCLED(3, "回收站");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(PmsWorkItemLifecycleStatusEnum::getStatus).toArray(Integer[]::new);

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

    public static PmsWorkItemLifecycleStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}
