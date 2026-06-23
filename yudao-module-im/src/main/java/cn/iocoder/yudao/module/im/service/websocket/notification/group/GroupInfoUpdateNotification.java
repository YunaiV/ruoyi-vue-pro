package cn.iocoder.yudao.module.im.service.websocket.notification.group;

import lombok.Data;

/**
 * 群信息变更事件通知（NAME / NOTICE 走独立事件）
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
    /**
     * 旧进群审批开关
     */
    private Boolean oldJoinApproval;
    /**
     * 新进群审批开关
     */
    private Boolean newJoinApproval;

}
