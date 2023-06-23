package cn.iocoder.yudao.module.promotion.enums.decorate;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.promotion.api.decorate.dto.*;
import lombok.Getter;

import java.lang.reflect.Type;
import java.util.List;

/**
 * 页面组件枚举
 *
 * @author jason
 */
@Getter
public enum PageComponentEnum {
    NAV_MENU("nav-menu", "导航菜单",
            new TypeReference<PageComponentDTO<NavMenuComponent.Config, CommonStyle, List<NavMenuComponent.DataStructure>>>() {}.getType()),
    ROLLING_BANNER("rolling-banner", "滚动横幅广告",
            new TypeReference<PageComponentDTO<RollingBannerComponent.Config, CommonStyle, List<RollingBannerComponent.DataStructure>>>() {}.getType()),
    PRODUCT_CATEGORY("product-category", "商品分类",
            new TypeReference<PageComponentDTO<ProductCategoryComponent.Config, CommonStyle, Object>>() {}.getType()); // data 会为null 用 object 代替

    /**
     * 页面组件代码
     */
    private final String code;

    /**
     * 页面组件说明
     */
    private final String desc;

    /**
     * 具体组件的类型
     */
    private final Type  componentType;

    PageComponentEnum(String code, String desc, Type componentType) {
        this.code = code;
        this.desc = desc;
        this.componentType = componentType;
    }

    public static PageComponentEnum getOfCode(String code) {
        return ArrayUtil.firstMatch(component -> component.getCode().equals(code), PageComponentEnum.values());
    }

    public <C, S, D> PageComponentDTO<C, S, D> parsePageComponent(String text) {
        return JsonUtils.parseObject(text, componentType);
    }
}
