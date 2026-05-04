package cn.iocoder.yudao.module.im.service.websocket.dto.notification.friend;

import lombok.Data;

/**
 * 收到新的好友申请通知
 * <p>
 * 推送给申请的接收方多端；前端拿到 requestId 后按需 fetch 完整申请记录，避免 payload 体积过大
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

}
