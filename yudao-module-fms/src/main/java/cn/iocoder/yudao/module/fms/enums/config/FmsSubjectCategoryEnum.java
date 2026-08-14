package cn.iocoder.yudao.module.fms.enums.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * FMS 科目类别枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsSubjectCategoryEnum {

    CURRENT_ASSET(1, 1, "流动资产"),
    NON_CURRENT_ASSET(1, 2, "非流动资产"),
    CURRENT_LIABILITY(2, 1, "流动负债"),
    NON_CURRENT_LIABILITY(2, 2, "非流动负债"),
    EQUITY(3, 1, "所有者权益"),
    COST(4, 1, "成本"),
    OPERATING_INCOME(5, 1, "营业收入"),
    OTHER_INCOME(5, 2, "其他收益"),
    PERIOD_EXPENSE(5, 3, "期间费用"),
    OTHER_LOSS(5, 4, "其他损失"),
    OPERATING_COST_AND_TAX(5, 5, "营业成本及税金"),
    PRIOR_YEAR_ADJUSTMENT(5, 6, "以前年度损益调整"),
    INCOME_TAX(5, 7, "所得税"),
    COMMON(6, 1, "共同");

    /**
     * 科目类型
     */
    private final Integer type;
    /**
     * 类别
     */
    private final Integer category;
    /**
     * 名字
     */
    private final String name;

    public static FmsSubjectCategoryEnum valueOf(Integer type, Integer category) {
        return Arrays.stream(values())
                .filter(item -> item.getType().equals(type) && item.getCategory().equals(category))
                .findFirst().orElse(null);
    }

    public static FmsSubjectCategoryEnum valueOfName(String name) {
        return Arrays.stream(values())
                .filter(item -> item.getName().equals(name))
                .findFirst().orElse(null);
    }

}
