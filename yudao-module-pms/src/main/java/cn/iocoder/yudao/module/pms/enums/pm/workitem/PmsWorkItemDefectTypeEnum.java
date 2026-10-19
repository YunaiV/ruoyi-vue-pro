package cn.iocoder.yudao.module.pms.enums.pm.workitem;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * PMS 缺陷类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PmsWorkItemDefectTypeEnum implements ArrayValuable<Integer> {

    FUNCTION(1, "功能问题"),
    UI(2, "界面优化"),
    USABILITY(3, "易用性问题"),
    SECURITY(4, "安全问题"),
    PERFORMANCE(5, "性能问题"),
    CODE(6, "代码错误");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(PmsWorkItemDefectTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 缺陷类型
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

    public static PmsWorkItemDefectTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
