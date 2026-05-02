package cn.iocoder.yudao.module.im.service.websocket.dto.notification;

import lombok.Data;

/**
 * 群主转让事件通知
 */
@Data
public class GroupOwnerTransferNotification {

    /**
     * 操作人（旧群主）用户编号
     */
    private Long operatorUserId;
    /**
     * 新群主用户编号
     */
    private Long newOwnerUserId;

}
