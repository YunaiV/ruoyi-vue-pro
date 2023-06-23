package cn.iocoder.yudao.module.promotion.enums.decorate;

import lombok.Getter;

/**
 * 页面组件枚举
 *
 * @author jason
 */
@Getter
public enum DecorateComponentEnum {
    NAV_MENU("nav-menu", "导航菜单"),
    ROLLING_BANNER("rolling-banner", "滚动横幅广告"),
    PRODUCT_CATEGORY("product-category", "商品分类");

    /**
     * 页面组件代码
     */
    private final String code;

    /**
     * 页面组件说明
     */
    private final String desc;

    DecorateComponentEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
