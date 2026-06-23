package cn.iocoder.yudao.module.im.service.websocket.notification.group;

import lombok.Data;

/**
 * 群主转让事件通知
 */
@Data
public class GroupOwnerTransferNotification extends BaseGroupNotification {

    /**
     * 新群主用户编号
     */
    private Long newOwnerUserId;

}
