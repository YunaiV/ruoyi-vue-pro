package cn.iocoder.yudao.module.system.api.message.user;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理员用户资料（昵称 / 头像）变更消息
 * <p>
 * 仅当 nickname 或 avatar 真的发生变化时才发送；订阅方据此做下游分发，
 * 例如 IM 模块向该用户的所有好友推送 FRIEND_INFO_UPDATED 通知
 *
 * @author 芋道源码
 */
@Data
public class AdminUserProfileUpdateMessage {

    /**
     * 用户编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;
    /**
     * 变更后的昵称（无变更时为 null）
     */
    private String nickname;
    /**
     * 变更后的头像（无变更时为 null）
     */
    private String avatar;

}
