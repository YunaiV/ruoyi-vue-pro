package cn.iocoder.yudao.module.system.api.social.dto;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信小程序订阅消息发送 Request DTO
 *
 * @author HUIHUI
 */
@Data
public class SocialWxSubscribeMessageSendReqDTO {

    /**
     * 用户 id
     *
     * 关联 MemberUserDO 的 id 编号
     * 关联 AdminUserDO 的 id 编号
     */
    private Long userId;
    /**
     * 用户类型, 预留 多商户转帐可能需要用到
     *
     * 关联 {@link UserTypeEnum}
     */
    private Integer userType;

    /**
     * 社交类型
     *
     * 枚举 {@link SocialTypeEnum}
     */
    private Integer socialType;

    /**
     * 消息模版标题
     */
    private String templateTitle;

    /**
     * 点击模板卡片后的跳转页面，仅限本小程序内的页面
     *
     * 支持带参数，（示例 index?foo=bar ）。该字段不填则模板无跳转。
     */
    private String page;

    /**
     * 模板内容的参数
     */
    private Map<String, String> messages;

    public SocialWxSubscribeMessageSendReqDTO addMessage(String key, String value) {
        if (messages == null) {
            messages = new HashMap<>();
        }
        messages.put(key, value);
        return this;
    }

}
