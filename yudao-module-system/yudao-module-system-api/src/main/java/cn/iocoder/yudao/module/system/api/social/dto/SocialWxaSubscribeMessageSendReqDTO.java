package cn.iocoder.yudao.module.system.api.social.dto;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信小程序订阅消息发送 Request DTO
 *
 * @author HUIHUI
 */
@Data
public class SocialWxaSubscribeMessageSendReqDTO {

    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 编号
     * 关联 AdminUserDO 的 id 编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;
    /**
     * 用户类型
     *
     * 关联 {@link UserTypeEnum}
     */
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    /**
     * 消息模版标题
     */
    @NotEmpty(message = "消息模版标题不能为空")
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

    public SocialWxaSubscribeMessageSendReqDTO addMessage(String key, String value) {
        if (messages == null) {
            messages = new HashMap<>();
        }
        messages.put(key, value);
        return this;
    }

}
