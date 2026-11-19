package cn.iocoder.yudao.module.hrm.enums.performance.plan;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 绩效指标评分类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmPerformanceQuotaScoreTypeEnum implements ArrayValuable<Integer> {

    /**
     * 直接输入：评分人手工填写得分
     */
    DIRECT_INPUT(1, "直接输入"),
    /**
     * 正向完成率：得分 = min(实际值 / 目标值, 1) × 统一评分上限
     */
    COMPLETION_RATE(2, "完成率");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmPerformanceQuotaScoreTypeEnum::getType).toArray(Integer[]::new);

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

    public static HrmPerformanceQuotaScoreTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    /**
     * 是否按完成率计算得分；为空或其他类型均按手工评分处理，兼容旧数据
     *
     * @param type 评分类型
     * @return 是否完成率
     */
    public static boolean isCompletionRate(Integer type) {
        return COMPLETION_RATE.getType().equals(type);
    }

}
