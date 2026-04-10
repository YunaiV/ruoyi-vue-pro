package cn.iocoder.yudao.module.mes.enums.cal;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 排班计划状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesCalPlanStatusEnum implements ArrayValuable<Integer> {

    /**
     * 草稿
     *
     * 对应 MesCalPlanService#createPlan 方法
     */
    PREPARE(0, "草稿"),
    /**
     * 已确认
     *
     * 对应 MesCalPlanService#confirmPlan 方法
     */
    CONFIRMED(1, "已确认");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesCalPlanStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
