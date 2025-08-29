package cn.iocoder.yudao.module.ai.framework.ai.core.webserch;

import lombok.Data;

import java.util.List;

@Data
public class AiWebSearchResponse {

    /**
     * 总数（总共匹配的网页数）
     */
    private Long total;

    /**
     * 数据列表
     */
    private List<WebPage> lists;

    /**
     * 网页对象
     */
    @Data
    public static class WebPage {

        /**
         * 名称
         *
         * 例如说：搜狐网
         */
        private String name;
        /**
         * 图标
         */
        private String icon;

        /**
         * 标题
         *
         * 例如说：186页|阿里巴巴：2024年环境、社会和治理（ESG）报告
         */
        private String title;
        /**
         * URL
         *
         * 例如说：https://m.sohu.com/a/815036254_121819701/?pvid=000115_3w_a
         */
        @SuppressWarnings("JavadocLinkAsPlainText")
        private String url;

        /**
         * 内容的简短描述
         */
        private String snippet;
        /**
         * 内容的文本摘要
         */
        private String summary;

    }

}