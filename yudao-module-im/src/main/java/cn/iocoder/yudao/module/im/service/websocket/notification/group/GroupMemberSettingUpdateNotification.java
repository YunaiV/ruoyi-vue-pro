package cn.iocoder.yudao.module.im.service.websocket.notification.group;

import lombok.Data;

/**
 * 群成员个人设置变更事件通知（个人多端同步）
 * <p>
 * silent / groupRemark 字段 null 表示本次未变更，前端按非 null 局部更新
 */
@Data
public class GroupMemberSettingUpdateNotification extends BaseGroupNotification {

    /**
     * 群免打扰
     */
    private Boolean silent;
    /**
     * 群备注
     */
    private String groupRemark;

}
