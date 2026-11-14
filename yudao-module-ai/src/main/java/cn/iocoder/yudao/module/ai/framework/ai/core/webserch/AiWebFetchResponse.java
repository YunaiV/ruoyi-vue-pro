package cn.iocoder.yudao.module.ai.framework.ai.core.webserch;

import lombok.Data;

/**
 * 网页读取结果
 *
 * @author 芋道源码
 */
@Data
public class AiWebFetchResponse {

    /**
     * 标题
     */
    private String title;
    /**
     * URL
     */
    private String url;
    /**
     * 正文内容
     */
    private String content;

}
