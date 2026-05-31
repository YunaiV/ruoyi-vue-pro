package cn.iocoder.yudao.module.im.service.websocket.dto.notification.group;

import lombok.Data;

/**
 * 群名变更事件通知
 */
@Data
public class GroupNameUpdateNotification extends BaseGroupNotification {

    /**
     * 旧群名
     */
    private String oldName;
    /**
     * 新群名
     */
    private String newName;

}
