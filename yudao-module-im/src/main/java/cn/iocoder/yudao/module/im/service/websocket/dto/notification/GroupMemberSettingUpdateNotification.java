package cn.iocoder.yudao.module.im.service.websocket.dto.notification;

import lombok.Data;

/**
 * 群成员个人设置变更事件通知（个人多端同步）
 */
@Data
public class GroupMemberSettingUpdateNotification {

    /**
     * 操作人（设置变更者本人）用户编号
     */
    private Long operatorUserId;
    /**
     * 群免打扰，null 表示本次未变更
     */
    private Boolean muted;
    /**
     * 群备注，null 表示本次未变更
     */
    private String groupRemark;

}
