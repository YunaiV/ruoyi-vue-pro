package cn.iocoder.yudao.module.hrm.enums.performance.plan;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 绩效评分可见内容枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmPerformanceReviewVisibleContentEnum implements ArrayValuable<Integer> {

    SELF(1, "仅自己的评分和评语"),
    ALL(2, "所有人的评分和评语");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmPerformanceReviewVisibleContentEnum::getContent).toArray(Integer[]::new);

    /**
     * 可见内容
     */
    private final Integer content;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmPerformanceReviewVisibleContentEnum valueOf(Integer content) {
        return ArrayUtil.firstMatch(item -> item.getContent().equals(content), values());
    }

}
