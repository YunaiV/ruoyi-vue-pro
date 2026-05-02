package cn.iocoder.yudao.module.im.service.websocket.dto.notification;

import lombok.Data;

/**
 * 成员组内昵称变更事件通知
 */
@Data
public class GroupMemberNicknameUpdateNotification {

    /**
     * 操作人（昵称变更者本人）用户编号
     */
    private Long operatorUserId;
    /**
     * 群内昵称
     */
    private String displayUserName;

}
