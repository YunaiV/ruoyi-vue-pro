package cn.iocoder.yudao.module.iot.enums;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 产品状态枚举类
 * 禁用 启用
 */
@AllArgsConstructor
@Getter
public enum ProductStatusEnum implements IntArrayValuable {

    DISABLE(0, "禁用"),
    ENABLE(1, "启用");

    /**
     * 类型
     */
    private final Integer type;

    /**
     * 描述
     */
    private final String description;

    public static final int[] ARRAYS = {1, 2};

    @Override
    public int[] array() {
        return ARRAYS;
    }
}
