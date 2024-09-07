package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IOT 产品状态枚举类
 * 产品状态, 0: 开发中, 1: 已发布
 */
@AllArgsConstructor
@Getter
public enum IotProductStatusEnum implements IntArrayValuable {

    UNPUBLISHED(0, "开发中"),
    PUBLISHED(1, "已发布");

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
