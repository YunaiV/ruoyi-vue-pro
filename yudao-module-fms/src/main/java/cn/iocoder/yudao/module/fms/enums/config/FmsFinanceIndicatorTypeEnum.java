package cn.iocoder.yudao.module.fms.enums.config;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

// TODO DONE @AI：JSON 预置文件已格式化，枚举已归入 config 包。
/**
 * FMS 首页财务指标取数报表类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsFinanceIndicatorTypeEnum implements ArrayValuable<Integer> {

    BALANCE_SHEET(1, "资产负债表"),
    INCOME_STATEMENT(2, "利润表");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(FmsFinanceIndicatorTypeEnum::getType).toArray(Integer[]::new);

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

    public static FmsFinanceIndicatorTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.type.equals(type), values());
    }

    public static boolean isBalanceSheet(Integer type) {
        return BALANCE_SHEET.getType().equals(type);
    }

    public static boolean isIncomeStatement(Integer type) {
        return INCOME_STATEMENT.getType().equals(type);
    }
}
