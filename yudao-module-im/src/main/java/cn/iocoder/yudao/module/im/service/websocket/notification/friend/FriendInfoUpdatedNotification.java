package cn.iocoder.yudao.module.im.service.websocket.notification.friend;

import lombok.Data;

/**
 * 好友资料变更通知（对端改了昵称 / 头像）
 * <p>
 * 由 AdminUserProfileUpdateConsumer 监听 system 模块的 AdminUserProfileUpdateMessage 后，批量推送给资料被改的人的所有好友；
 * 前端 dispatcher 收到后调 loadFriendInfo 重拉资料
 * <p>
 * 此处 friendUserId 表示「资料被更新的那个人」（即对端），与 operatorUserId 一致
 */
@Data
public class FriendInfoUpdatedNotification extends BaseFriendNotification {

}
