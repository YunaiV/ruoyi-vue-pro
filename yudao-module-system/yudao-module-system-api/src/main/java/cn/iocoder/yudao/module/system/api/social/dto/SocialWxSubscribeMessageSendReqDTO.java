package cn.iocoder.yudao.module.system.api.social.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信小程序订阅消息发送 Request DTO
 *
 * @see <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/subscribe-message/subscribeMessage.send.html">接口文档</a>
 * @author HUIHUI
 */
@Data
public class SocialWxSubscribeMessageSendReqDTO {

    /**
     * 接收者（用户）的 openid.
     * <pre>
     * 参数：touser
     * 是否必填： 是
     * 描述： 接收者（用户）的 openid
     * </pre>
     */
    @NotNull(message = "接收者（用户）的 openid不能为空")
    private String toUser;

    /**
     * 所需下发的模板消息的id.
     * <pre>
     * 参数：template_id
     * 是否必填： 是
     * 描述： 所需下发的模板消息的id
     * </pre>
     */
    @NotNull(message = "模板消息的id不能为空")
    private String templateId;

    /**
     * 点击模板卡片后的跳转页面，仅限本小程序内的页面.
     * <pre>
     * 参数：page
     * 是否必填： 否
     * 描述： 点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转。
     * </pre>
     */
    private String page;

    /**
     * 跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
     *
     * 枚举 WxMaConstants.MiniProgramState
     */
    @NotNull(message = "跳转小程序类型不能为空")
    private String miniprogramState;

    /**
     * 进入小程序查看的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
     *
     * 枚举 WxMaConstants.MiniProgramLang
     */
    @NotNull(message = "进入小程序查看的语言类型不能为空")
    private String lang;

    /**
     * 模板内容，不填则下发空模板.
     * <pre>
     * 参数：data
     * 是否必填： 是
     * 描述： 模板内容，不填则下发空模板
     * </pre>
     */
    private Map<String, String> messages;

}
