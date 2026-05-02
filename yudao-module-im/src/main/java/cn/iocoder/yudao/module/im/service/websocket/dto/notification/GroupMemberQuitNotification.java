package cn.iocoder.yudao.module.im.service.websocket.dto.notification;

import lombok.Data;

/**
 * 成员退群事件通知
 */
@Data
public class GroupMemberQuitNotification {

    /**
     * 操作人（退群者本人）用户编号
     */
    private Long operatorUserId;

}
