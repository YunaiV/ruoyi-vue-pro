package cn.iocoder.dashboard.modules.system.controller.auth.vo;

import lombok.Data;

import java.util.List;

/**
 * 路由配置信息
 *
 * @author ruoyi
 */
@Data
public class SysAuthGetRouterRespVO {

    /**
     * 菜单编号
     */
    private Long menuId;
    /**
     * 父菜单编号
     */
    private Long parentId;

    /**
     * 路由名字
     */
    private String name;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 是否隐藏路由，当设置 true 的时候该路由不会再侧边栏出现
     */
    private boolean hidden;

    /**
     * 重定向地址，当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
     */
    private String redirect;

    /**
     * 组件地址
     */
    private String component;

    /**
     * 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
     */
    private Boolean alwaysShow;

    /**
     * 其他元素
     */
    private MetaVO meta;

    /**
     * 子路由
     */
    private List<SysAuthGetRouterRespVO> children;

    @Data
    public static class MetaVO {

        /**
         * 设置该路由在侧边栏和面包屑中展示的名字
         */
        private String title;

        /**
         * 设置该路由的图标，对应路径src/assets/icons/svg
         */
        private String icon;

        /**
         * 设置为true，则不会被 <keep-alive>缓存
         */
        private boolean noCache;

    }

}
