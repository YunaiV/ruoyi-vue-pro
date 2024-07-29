package cn.iocoder.yudao.module.system.api.social.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 微信小程序订阅消息发送 Request DTO
 *
 * @see <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/subscribe-message/subscribeMessage.send.html">接口文档</a>
 * @author HUIHUI
 */
@Data
public class SocialWxSubscribeMessageSendReqDTO {

    // TODO @puhui999：貌似使用 userId + userType 会不会更合理哈。这样，后端进行查询三方用户的绑定表~
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
     * 模版消息编号
     */
    @NotNull(message = "模版消息编号不能为空")
    private String templateId;

    /**
     * 点击模板卡片后的跳转页面，仅限本小程序内的页面
     *
     * 支持带参数，（示例 index?foo=bar ）。该字段不填则模板无跳转。
     */
    private String page;

    /**
     * 跳转小程序类型
     *
     * developer 为开发版；trial 为体验版；formal 为正式版【默认】
     *
     * 枚举 WxMaConstants.MiniProgramState
     */
    // TODO @puhui999：这个非必填。如果没有，代码里去默认下；
    @NotNull(message = "跳转小程序类型不能为空")
    private String miniprogramState;

    /**
     * 进入小程序查看的语言类型
     *
     * zh_CN(简体中文)【默认】、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)
     *
     * 枚举 WxMaConstants.MiniProgramLang
     */
    // TODO @puhui999：这个非必填。如果没有，代码里去默认下；
    @NotNull(message = "进入小程序查看的语言类型不能为空")
    private String lang;

    /**
     * 模板内容的参数
     */
    private Map<String, String> messages;

}
