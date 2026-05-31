package cn.iocoder.yudao.module.im.service.websocket.dto.notification.group;

import lombok.Data;

/**
 * 群信息变更事件通知（当前承载头像变更，NAME / NOTICE 走独立事件）
 */
@Data
public class GroupInfoUpdateNotification extends BaseGroupNotification {

    /**
     * 旧群头像
     */
    private String oldAvatar;
    /**
     * 新群头像
     */
    private String newAvatar;

}
