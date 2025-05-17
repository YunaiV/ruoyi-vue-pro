package cn.iocoder.yudao.module.promotion.enums.diy;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
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
public enum DiyPageEnum implements ArrayValuable<Integer> {

    INDEX(1, "首页"),
    MY(2, "我的"),
    ;

    private static final Integer[] ARRAYS = Arrays.stream(values()).map(DiyPageEnum::getPage).toArray(Integer[]::new);

    /**
     * 页面编号
     */
    private final Integer page;

    /**
     * 页面名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
