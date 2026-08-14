package cn.iocoder.yudao.module.fms.enums.closing;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * FMS 结账模板分类枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsClosingTemplateCategoryEnum implements ArrayValuable<Integer> {

    DAILY_EXPENSE(1, "日常开支"),
    PURCHASE_SALE(2, "采购销售"),
    CURRENT_ACCOUNT(3, "往来款"),
    TRANSFER_BUSINESS(4, "转账业务");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(FmsClosingTemplateCategoryEnum::getCategory).toArray(Integer[]::new);

    /**
     * 分类
     */
    private final Integer category;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
