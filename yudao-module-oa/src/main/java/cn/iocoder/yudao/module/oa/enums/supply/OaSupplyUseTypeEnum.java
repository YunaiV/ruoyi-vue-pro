package cn.iocoder.yudao.module.oa.enums.supply;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用品使用类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaSupplyUseTypeEnum implements ArrayValuable<Integer> {

    RECEIVE(1, "领用"),
    BORROW(2, "借用");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaSupplyUseTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 用品使用类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    public static OaSupplyUseTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
