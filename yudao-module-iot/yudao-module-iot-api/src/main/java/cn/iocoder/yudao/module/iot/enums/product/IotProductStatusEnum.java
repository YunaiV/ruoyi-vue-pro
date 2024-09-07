package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
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
public enum IotProductStatusEnum implements IntArrayValuable {

    UNPUBLISHED(0, "开发中"),
    PUBLISHED(1, "已发布");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotProductStatusEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String description;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
