package cn.iocoder.yudao.module.im.service.websocket.notification.group;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 入群申请被拒绝通知
 * <p>
 * 定向推送给申请人 + 群主 + 全部管理员；admin 侧据此把 unhandledCount 减 1 并从未处理列表移除
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GroupRequestRejectedNotification extends BaseGroupNotification {

    /**
     * 已处理的申请记录编号
     */
    private Long requestId;
    /**
     * 群编号
     */
    private Long groupId;
    /**
     * 申请人 / 被邀请人用户编号
     */
    private Long userId;
    /**
     * 拒绝理由（可选）
     */
    private String handleContent;

}
