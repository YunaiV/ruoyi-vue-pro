package cn.iocoder.yudao.module.hrm.enums.employee.info;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 员工导入重复数据处理策略枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmEmployeeImportDuplicateStrategyEnum implements ArrayValuable<Integer> {

    SKIP(1, "跳过"),
    UPDATE(2, "覆盖"),
    FAIL(3, "判失败");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmEmployeeImportDuplicateStrategyEnum::getStrategy).toArray(Integer[]::new);

    private final Integer strategy;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmEmployeeImportDuplicateStrategyEnum valueOf(Integer strategy) {
        return ArrayUtil.firstMatch(item -> item.getStrategy().equals(strategy), values());
    }

}
