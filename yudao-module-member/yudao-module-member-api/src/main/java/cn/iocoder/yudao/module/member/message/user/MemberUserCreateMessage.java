package cn.iocoder.yudao.module.member.message.user;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 会员用户创建消息
 *
 * @author owen
 */
@Data
public class MemberUserCreateMessage {

    /**
     * 用户编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;

}
