package cn.iocoder.yudao.module.im.service.websocket.dto.notification.group;

import lombok.Data;

/**
 * 群事件通知基类：所有群事件 payload 共享 operatorUserId
 */
@Data
public abstract class BaseGroupNotification {

    /**
     * 操作人用户编号
     */
    private Long operatorUserId;

}
