package cn.iocoder.yudao.module.promotion.enums.decorate;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 装修页面枚举
 *
 * @author jason
 */
@AllArgsConstructor
@Getter
public enum DecoratePageEnum implements IntArrayValuable {

    INDEX(1, "首页");

    private static final int[] ARRAYS = Arrays.stream(values()).mapToInt(DecoratePageEnum::getId).toArray();

    /**
     * 页面 id
     */
    private final Integer id;
    /**
     * 页面名称
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
