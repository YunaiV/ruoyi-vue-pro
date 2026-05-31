package cn.iocoder.yudao.module.im.service.websocket.dto.notification.friend;

import lombok.Data;

/**
 * 好友删除通知
 * <p>
 * 仅推送给操作人多端做同步（对端不感知，与单边删除语义对齐）；前端清除本地好友 + 按 clear 决定级联清理
 */
@Data
public class FriendDeleteNotification extends BaseFriendNotification {

    /**
     * 是否级联清理本端相关数据（当前包含私聊会话；未来可能扩展更多 clear 项）
     */
    private Boolean clear;

}
