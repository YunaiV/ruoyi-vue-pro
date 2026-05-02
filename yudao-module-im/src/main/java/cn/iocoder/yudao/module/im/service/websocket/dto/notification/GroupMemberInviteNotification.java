package cn.iocoder.yudao.module.im.service.websocket.dto.notification;

import lombok.Data;

import java.util.List;

/**
 * 成员加入事件通知
 */
@Data
public class GroupMemberInviteNotification {

    /**
     * 操作人（邀请人）用户编号
     */
    private Long operatorUserId;
    /**
     * 被邀请人用户编号列表
     */
    private List<Long> memberUserIds;

}
