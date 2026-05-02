package cn.iocoder.yudao.module.im.service.websocket.dto.notification;

import lombok.Data;

/**
 * 群公告变更事件通知
 */
@Data
public class GroupNoticeUpdateNotification {

    /**
     * 操作人用户编号
     */
    private Long operatorUserId;
    /**
     * 旧群公告
     */
    private String oldNotice;
    /**
     * 新群公告
     */
    private String newNotice;

}
