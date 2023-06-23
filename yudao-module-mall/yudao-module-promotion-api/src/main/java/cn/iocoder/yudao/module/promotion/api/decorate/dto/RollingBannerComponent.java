package cn.iocoder.yudao.module.promotion.api.decorate.dto;

import lombok.Data;

/**
 * 滚动横幅广告组件的内容配置,数据结构定义
 *
 * @author jason
 */
public class RollingBannerComponent {
    /**
     * 滚动横幅广告组件的内容配置项
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
