package cn.iocoder.yudao.module.im.service.websocket.notification.friend;

import lombok.Data;

/**
 * 好友事件通知基类
 * <p>
 * 所有好友事件 payload 共享：
 * - operatorUserId 标识"谁触发的"，用于多端同步时本端识别"是我自己触发的"还是"对方触发的"
 * - friendUserId 标识"好友是谁"，前端按它定位 / 更新本地 friend 缓存
 */
@Data
public abstract class BaseFriendNotification {

    /**
     * 操作人用户编号
     */
    private Long operatorUserId;
    /**
     * 好友用户编号
     */
    private Long friendUserId;

}
