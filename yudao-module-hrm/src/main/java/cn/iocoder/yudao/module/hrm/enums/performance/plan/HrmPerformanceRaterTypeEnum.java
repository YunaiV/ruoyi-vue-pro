package cn.iocoder.yudao.module.hrm.enums.performance.plan;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 绩效评分人类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmPerformanceRaterTypeEnum implements ArrayValuable<Integer> {

    SUPERIOR(1, "上级"),
    DEPT_LEADER(2, "部门负责人"),
    SPECIFIED(3, "指定评分人"),
    SELF(4, "被考核人");

    /**
     * 上级、部门负责人支持的最小层级
     */
    public static final int MIN_LEVEL = 1;
    /**
     * 上级、部门负责人支持的最大层级，与绩效计划保存校验保持一致
     */
    public static final int MAX_LEVEL = 10;

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmPerformanceRaterTypeEnum::getType).toArray(Integer[]::new);

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

    public static HrmPerformanceRaterTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
