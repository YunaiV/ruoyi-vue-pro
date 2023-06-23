package cn.iocoder.yudao.module.promotion.enums.decorate;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 装修页面枚举
 *
 * @author jason
 */
@AllArgsConstructor
@Getter
public enum DecoratePageTypeEnum {
    INDEX(1, "首页");

    private final Integer type;
    /**
     * 名字
     */
    private final String name;
}
