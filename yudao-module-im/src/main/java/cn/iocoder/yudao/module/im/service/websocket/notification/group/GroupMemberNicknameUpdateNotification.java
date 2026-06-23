package cn.iocoder.yudao.module.im.service.websocket.notification.group;

import lombok.Data;

/**
 * 成员组内昵称变更事件通知
 */
@Data
public class GroupMemberNicknameUpdateNotification extends BaseGroupNotification {

    /**
     * 群内昵称
     */
    private String displayUserName;

}
