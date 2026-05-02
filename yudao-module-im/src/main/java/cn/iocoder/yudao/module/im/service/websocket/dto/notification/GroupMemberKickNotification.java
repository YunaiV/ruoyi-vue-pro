package cn.iocoder.yudao.module.im.service.websocket.dto.notification;

import lombok.Data;

import java.util.List;

/**
 * 成员被移出事件通知
 */
@Data
public class GroupMemberKickNotification {

    /**
     * 操作人（群主 / 管理员）用户编号
     */
    private Long operatorUserId;
    /**
     * 被移出的成员用户编号列表
     */
    private List<Long> memberUserIds;

}
