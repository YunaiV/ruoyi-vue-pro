package cn.iocoder.yudao.module.im.service.websocket.notification.group;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 取消成员禁言通知
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GroupMemberCancelMutedNotification extends BaseGroupNotification {

    /**
     * 被取消禁言的用户编号
     */
    private Long mutedUserId;

}
