package cn.iocoder.yudao.module.im.service.websocket.dto.notification;

import lombok.Data;

import java.util.List;

/**
 * 撤销管理员事件通知
 */
@Data
public class GroupAdminRemoveNotification {

    /**
     * 操作人（群主）用户编号
     */
    private Long operatorUserId;
    /**
     * 被撤销管理员的成员用户编号列表
     */
    private List<Long> memberUserIds;

}
