package cn.iocoder.yudao.module.promotion.enums.decorate;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 装修页面类型枚举
 *
 * @author jason
 */
@AllArgsConstructor
@Getter
public enum DecoratePageTypeEnum implements IntArrayValuable {

    INDEX(1, "首页");

    private static final int[] ARRAYS = Arrays.stream(values()).mapToInt(DecoratePageTypeEnum::getType).toArray();

    /**
     * 页面类型
     */
    private final Integer type;
    /**
     * 页面名称
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
