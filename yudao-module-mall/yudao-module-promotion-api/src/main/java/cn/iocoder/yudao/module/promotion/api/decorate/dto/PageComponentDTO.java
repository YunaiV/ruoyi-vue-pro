package cn.iocoder.yudao.module.promotion.api.decorate.dto;

import lombok.Data;

/**
 * 页面组件通用模板结构 DTO
 * 每个组件都有自己对应的内容配置， 样式配置， 具体数据，和其他组件不同的。但这个三个配置是必须的， 所以定义通用的模板结构
 * 内容配置， 样式配置， 具体数据的结构 需要每个组件单独定义。
 * 1. 内容配置 (不包括具体的内容) 例如是否启用，是否显示， 商品分类页的排版方式等
 * 2. 样式设置,对应 crmeb php 版组件的样式配置。 暂时可能用不。 所以使用通用 CommonStyleDTO，后续可能用上
 * 3. 具体数据, 有些组件(导航菜单组件)数据是通过装修配置, 需要定制数据。有些组件(如商品分类），数据从接口获取，不需要该项
 *
 * @author jason
 */
@Data
public class PageComponentDTO<Config, Style, Content> {

    /**
     * 组件标题， 每个组件都有的
     */
    private String title;

    /**
     * 组件的内容配置项
     */
    private Config config;

    /**
     * 组件的样式配置
     */
    private Style style;

    /**
     * 组件的具体数据
     *
     */
    private Content data;

}
