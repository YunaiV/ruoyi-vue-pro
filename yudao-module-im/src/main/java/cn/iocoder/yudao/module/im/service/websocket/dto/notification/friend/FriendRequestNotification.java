package cn.iocoder.yudao.module.im.service.websocket.dto.notification.friend;

import lombok.Data;

/**
 * 收到新的好友申请通知
 * <p>
 * 推送给申请的接收方多端；payload 已携带 fromUser 聚合字段，前端按 requestId 直接 push 进列表，无需回拉
 */
@Data
public class FriendRequestNotification extends BaseFriendNotification {

    /**
     * 申请记录编号
     */
    private Long requestId;
    /**
     * 申请理由
     */
    private String applyContent;
    /**
     * 添加来源
     */
    private Integer addSource;

    // ========== 聚合自 AdminUser，避免前端再调 system 接口 ==========

    /**
     * 申请方昵称
     */
    private String fromNickname;
    /**
     * 申请方头像
     */
    private String fromAvatar;

}
