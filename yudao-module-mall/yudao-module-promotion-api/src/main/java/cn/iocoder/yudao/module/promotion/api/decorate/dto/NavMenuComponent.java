package cn.iocoder.yudao.module.promotion.api.decorate.dto;

import lombok.Data;

/**
 * 导航菜单组件的内容配置,数据结构 定义
 * 不知道放哪里比较好 // TODO @芋艿 貌似放 api 这里不太合适。有什么建议没有？
 *
 * @author jason
 */
@Data
public class NavMenuComponent {

    /**
     * 导航菜单组件对内容的配置项
     */
    @Data
    public static class Config {

        /**
         * 是否启用
         */
        private Boolean enabled;
    }

    /**
     * 导航菜单组件的数据结构
     */
    @Data
    public static class DataStructure {
        /**
         * 显示名称
         */
        private String name;

        /**
         * 显示图片
         */
        private String img;

        /**
         * 连接路径
         */
        private String path;

        /**
         * 状态, 是否显示
         */
        private Integer status;

    }
}
