package cn.iocoder.yudao.module.im.service.websocket.dto.notification;

import lombok.Data;

import java.util.List;

/**
 * 添加管理员事件通知
 */
@Data
public class GroupAdminAddNotification {

    /**
     * 操作人（群主）用户编号
     */
    private Long operatorUserId;
    /**
     * 被设为管理员的成员用户编号列表
     */
    private List<Long> memberUserIds;

}
