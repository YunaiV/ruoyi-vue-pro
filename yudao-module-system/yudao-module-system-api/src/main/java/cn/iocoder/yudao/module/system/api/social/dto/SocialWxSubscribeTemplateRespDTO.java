package cn.iocoder.yudao.module.system.api.social.dto;

import lombok.Data;


/**
 * 小程序订阅消息模版 Response DTO
 *
 * @author HUIHUI
 */
@Data
public class SocialWxSubscribeTemplateRespDTO {

    // TODO @puhui999：建议搞成 id
    /**
     * 添加至账号下的模板 id，发送小程序订阅消息时所需
     */
    private String priTmplId;

    /**
     * 模版标题
     */
    private String title;

    /**
     * 模版内容
     */
    private String content;

    /**
     * 模板内容示例
     */
    private String example;

    // TODO @puhui999：这个在 wxjava 里面，有枚举字段么？
    /**
     * 模版类型
     *
     * 2 为一次性订阅
     * 3 为长期订阅
     */
    private Integer type;

}
