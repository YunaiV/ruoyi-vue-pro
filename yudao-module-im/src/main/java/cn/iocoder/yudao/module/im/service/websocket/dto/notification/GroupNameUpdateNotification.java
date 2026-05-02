package cn.iocoder.yudao.module.im.service.websocket.dto.notification;

import lombok.Data;

/**
 * 群名变更事件通知
 */
@Data
public class GroupNameUpdateNotification {

    /**
     * 操作人用户编号
     */
    private Long operatorUserId;
    /**
     * 旧群名
     */
    private String oldName;
    /**
     * 新群名
     */
    private String newName;

}
