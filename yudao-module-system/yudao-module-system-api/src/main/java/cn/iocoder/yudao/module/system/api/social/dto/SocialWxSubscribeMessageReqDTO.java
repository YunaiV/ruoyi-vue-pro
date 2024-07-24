package cn.iocoder.yudao.module.system.api.social.dto;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 微信小程序订阅消息 Request DTO
 *
 * @author HUIHUI
 */
@Data
public class SocialWxSubscribeMessageReqDTO {

    /**
     * 接收者（用户）的 openid.
     * <pre>
     * 参数：touser
     * 是否必填： 是
     * 描述： 接收者（用户）的 openid
     * </pre>
     */
    private String toUser;

    /**
     * 所需下发的模板消息的id.
     * <pre>
     * 参数：template_id
     * 是否必填： 是
     * 描述： 所需下发的模板消息的id
     * </pre>
     */
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
    private String miniprogramState;

    /**
     * 进入小程序查看的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
     *
     * 枚举 WxMaConstants.MiniProgramLang
     */
    private String lang;

    /**
     * 模板内容，不填则下发空模板.
     * <pre>
     * 参数：data
     * 是否必填： 是
     * 描述： 模板内容，不填则下发空模板
     * </pre>
     */
    private List<KeyValue<String, String>> messages;

    public SocialWxSubscribeMessageReqDTO addData(String key, String value) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(new KeyValue<>(key, value));
        return this;
    }

}
