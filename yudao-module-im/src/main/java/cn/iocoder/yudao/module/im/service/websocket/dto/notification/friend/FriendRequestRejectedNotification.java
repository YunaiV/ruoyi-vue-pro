package cn.iocoder.yudao.module.im.service.websocket.dto.notification.friend;

import lombok.Data;

/**
 * 好友申请被拒绝通知
 * <p>
 * 推送给原申请发起方多端；前端按 requestId 把对应申请记录 handleResult 更新为「已拒绝」
 */
@Data
public class FriendRequestRejectedNotification extends BaseFriendNotification {

    /**
     * 已处理的申请记录编号
     */
    private Long requestId;
    /**
     * 拒绝理由（可选）
     */
    private String handleContent;

}
