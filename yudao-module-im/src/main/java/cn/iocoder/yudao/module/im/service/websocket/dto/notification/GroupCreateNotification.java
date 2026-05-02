package cn.iocoder.yudao.module.im.service.websocket.dto.notification;

import lombok.Data;

import java.util.List;

/**
 * 群创建事件通知
 */
@Data
public class GroupCreateNotification {

    /**
     * 操作人用户编号
     */
    private Long operatorUserId;
    /**
     * 群成员用户编号列表（含创建者 + 初始邀请成员）
     */
    private List<Long> memberUserIds;

}
