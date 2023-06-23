package cn.iocoder.yudao.module.promotion.api.decorate.dto;

import lombok.Data;

/**
 * 商品分类组件的内容配置, 无数据结构定义。数据从接口获取
 *
 * @author jason
 */
public class ProductCategoryComponent {

    /**
     * 商品分类组件组件的内容配置项
     */
    @Data
    public static class Config {

        /**
         * 页面风格类型
         */
        private Integer layoutStyle;

        // TODO 其它
    }
}
