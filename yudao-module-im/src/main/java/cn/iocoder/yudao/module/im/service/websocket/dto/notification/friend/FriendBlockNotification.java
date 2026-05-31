package cn.iocoder.yudao.module.im.service.websocket.dto.notification.friend;

import lombok.Data;

/**
 * 加入黑名单通知
 * <p>
 * A 拉黑 B 后仅推 A 多端；B 端不感知（B 那边 blocked=0 不变）
 */
@Data
public class FriendBlockNotification extends BaseFriendNotification {

}
