package cn.iocoder.yudao.module.im.service.websocket.dto.notification.group;

import lombok.Data;

/**
 * 群封禁 / 解封通知
 */
@Data
public class GroupBannedNotification extends BaseGroupNotification {

    /**
     * 是否封禁
     */
    private Boolean banned;

}
