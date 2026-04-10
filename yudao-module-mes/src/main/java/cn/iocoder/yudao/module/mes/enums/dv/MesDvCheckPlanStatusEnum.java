package cn.iocoder.yudao.module.mes.enums.dv;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 点检保养方案状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesDvCheckPlanStatusEnum implements ArrayValuable<Integer> {

    /**
     * 草稿
     *
     * 对应 MesDvCheckPlanService#createCheckPlan 方法
     */
    PREPARE(0, "草稿"),
    /**
     * 已启用
     *
     * 对应 MesDvCheckPlanService#enableCheckPlan 方法
     */
    ENABLED(1, "已启用");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesDvCheckPlanStatusEnum::getStatus).toArray(Integer[]::new);

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
