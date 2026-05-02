package cn.iocoder.yudao.module.im.service.websocket.dto.notification;

import lombok.Data;

/**
 * 群解散事件通知
 */
@Data
public class GroupDissolveNotification {

    /**
     * 操作人（群主）用户编号
     */
    private Long operatorUserId;

}
