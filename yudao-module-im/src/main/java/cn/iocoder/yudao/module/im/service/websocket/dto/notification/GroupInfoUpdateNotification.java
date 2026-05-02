package cn.iocoder.yudao.module.im.service.websocket.dto.notification;

import lombok.Data;

/**
 * 群信息变更事件通知（NAME / NOTICE 之外字段的兜底）
 */
@Data
public class GroupInfoUpdateNotification {

    /**
     * 操作人用户编号
     */
    private Long operatorUserId;
    /**
     * 旧群头像
     */
    private String oldAvatar;
    /**
     * 新群头像
     */
    private String newAvatar;

}
