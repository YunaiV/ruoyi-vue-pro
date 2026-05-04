package cn.iocoder.yudao.module.im.service.websocket.dto.notification.friend;

import lombok.Data;

/**
 * 好友删除通知
 * <p>
 * 仅推送给操作人多端做同步（对端不感知，与单边删除语义对齐）；前端清除本地好友 + 级联清理私聊会话
 */
@Data
public class FriendDeleteNotification extends BaseFriendNotification {

}
