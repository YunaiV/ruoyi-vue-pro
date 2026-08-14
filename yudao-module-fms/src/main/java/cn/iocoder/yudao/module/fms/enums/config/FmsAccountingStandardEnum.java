package cn.iocoder.yudao.module.fms.enums.config;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * FMS 会计制度枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsAccountingStandardEnum implements ArrayValuable<Integer> {

    SMALL_BUSINESS_2013(1, "小企业会计准则（2013 年颁）");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(FmsAccountingStandardEnum::getStandard).toArray(Integer[]::new);

    /**
     * 会计制度
     */
    private final Integer standard;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static FmsAccountingStandardEnum valueOf(Integer standard) {
        return ArrayUtil.firstMatch(item -> item.getStandard().equals(standard), values());
    }

}
