package cn.iocoder.yudao.module.promotion.enums.diy;

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
public enum DiyPageEnum implements IntArrayValuable {

    INDEX(1, "首页"),
    MY(2, "我的"),
    ;

    private static final int[] ARRAYS = Arrays.stream(values()).mapToInt(DiyPageEnum::getPage).toArray();

    /**
     * 页面编号
     */
    private final Integer page;

    /**
     * 页面名称
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
