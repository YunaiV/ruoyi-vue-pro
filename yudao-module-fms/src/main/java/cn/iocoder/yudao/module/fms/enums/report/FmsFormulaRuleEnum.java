package cn.iocoder.yudao.module.fms.enums.report;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * FMS 报表取数规则枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsFormulaRuleEnum implements ArrayValuable<Integer> {

    BALANCE(0, "余额"),
    DEBIT_BALANCE(1, "借方余额"),
    CREDIT_BALANCE(2, "贷方余额"),
    SUBJECT_DEBIT_BALANCE(3, "科目借方余额"),
    SUBJECT_CREDIT_BALANCE(4, "科目贷方余额"),
    DEBIT_AMOUNT(5, "借方发生额"),
    CREDIT_AMOUNT(6, "贷方发生额"),
    PROFIT_LOSS_AMOUNT(7, "损益发生额");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(FmsFormulaRuleEnum::getRule).toArray(Integer[]::new);

    /**
     * 规则
     */
    private final Integer rule;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static FmsFormulaRuleEnum valueOf(Integer rule) {
        return ArrayUtil.firstMatch(item -> item.getRule().equals(rule), values());
    }

}
