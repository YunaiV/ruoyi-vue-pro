package cn.iocoder.yudao.module.promotion.enums.decorate;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 页面组件枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("JavadocLinkAsPlainText")
public enum DecorateComponentEnum {

    /**
     * 格式：[{
     *  "name": "标题"
     *  "picUrl": "https://www.iocoder.cn/xxx.png",
     *  "url": "/pages/users/index"
     * }]
     *
     * 最多 10 个
     */
    MENU("menu", "菜单"),
    /**
     * 格式：[{
     *  "name": "标题"
     *  "url": "/pages/users/index"
     * }]
     */
    ROLLING_NEWS("scrolling-news", "滚动新闻"),
    /**
     * 格式：[{
     *  "picUrl": "https://www.iocoder.cn/xxx.png",
     *  "url": "/pages/users/index"
     * }]
     */
    SLIDE_SHOW("slide-show", "轮播图"),
    /**
     * 格式：[{
     *  "name": "标题"
     *  "type": "类型", // best、hot、new、benefit、good
     *  "tag": "标签" // 例如说：多买多省
     * }]
     *
     * 最多 4 个
     */
    PRODUCT_RECOMMEND("product-recommend", "商品推荐");

    /**
     * 页面组件代码
     */
    private final String code;

    /**
     * 页面组件说明
     */
    private final String desc;

}
