package cn.iocoder.yudao.module.im.service.websocket.dto.notification.friend;

import lombok.Data;

// TODO @AI：1209 暂未实现；好友信息在 system 模块，需要 system 改昵称 / 头像后发 ApplicationEvent，IM 模块监听后回调发送此通知
/**
 * 好友资料变更通知（对端改了昵称 / 头像）
 * <p>
 * 推送给资料被改的人的所有好友；前端清缓存 + 重拉资料
 * <p>
 * 此处 friendUserId 表示「资料被更新的那个人」（即对端），与 operatorUserId 一致
 */
@Data
public class FriendInfoUpdatedNotification extends BaseFriendNotification {

}
