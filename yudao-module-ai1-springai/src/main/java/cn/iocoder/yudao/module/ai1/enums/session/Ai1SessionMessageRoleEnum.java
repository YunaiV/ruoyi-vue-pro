package cn.iocoder.yudao.module.ai1.enums.session;

import cn.hutool.core.util.ObjUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI1 会话消息角色的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum Ai1SessionMessageRoleEnum {

    USER("user", "用户"),
    ASSISTANT("assistant", "助手");

    /**
     * 角色
     */
    private final String role;
    /**
     * 名称
     */
    private final String name;

    public static boolean isUser(String role) {
        return ObjUtil.equal(USER.role, role);
    }

    public static boolean isAssistant(String role) {
        return ObjUtil.equal(ASSISTANT.role, role);
    }

}
