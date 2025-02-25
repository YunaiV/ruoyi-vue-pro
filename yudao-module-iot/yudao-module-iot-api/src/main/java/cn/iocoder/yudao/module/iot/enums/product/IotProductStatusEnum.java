package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IOT 产品的状态枚举类
 *
 * @author ahh
 */
@AllArgsConstructor
@Getter
public enum IotProductStatusEnum implements ArrayValuable<Integer> {

    UNPUBLISHED(0, "开发中"),
    PUBLISHED(1, "已发布");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotProductStatusEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String description;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
