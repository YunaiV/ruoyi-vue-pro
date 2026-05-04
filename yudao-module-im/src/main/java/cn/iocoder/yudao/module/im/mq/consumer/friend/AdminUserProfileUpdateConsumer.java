package cn.iocoder.yudao.module.im.mq.consumer.friend;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import cn.iocoder.yudao.module.im.service.websocket.dto.notification.friend.FriendInfoUpdatedNotification;
import cn.iocoder.yudao.module.system.api.message.user.AdminUserProfileUpdateMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 监听 system 模块的 {@link AdminUserProfileUpdateMessage} 消息，向「资料被改的人」的所有好友推送 FRIEND_INFO_UPDATED 通知
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class AdminUserProfileUpdateConsumer {

    @Resource
    private ImFriendService friendService;
    @Resource
    private ImWebSocketService websocketService;

    @EventListener
    @Async // Spring Event 默认在 Producer 发送的线程，通过 @Async 实现异步
    public void onMessage(AdminUserProfileUpdateMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        Long userId = message.getUserId();
        // 1. 找当前用户的所有有效好友（DISABLE 已删的不推）
        List<ImFriendDO> friends = friendService.getFriendList(userId);
        if (friends.isEmpty()) {
            return;
        }

        // 2. 给每个好友的多端推 FRIEND_INFO_UPDATED；payload 里 operatorUserId / friendUserId 都是「资料被改的人」
        int notifyCount = 0;
        for (ImFriendDO friend : friends) {
            if (CommonStatusEnum.isDisable(friend.getStatus())) {
                continue;
            }
            FriendInfoUpdatedNotification payload = (FriendInfoUpdatedNotification) new FriendInfoUpdatedNotification()
                    .setOperatorUserId(userId).setFriendUserId(userId);
            websocketService.sendPrivateMessageAsync(friend.getFriendUserId(), ImPrivateMessageDTO.ofFriendNotification(
                    ImMessageTypeEnum.FRIEND_INFO_UPDATED.getType(), userId, friend.getFriendUserId(), payload));
            notifyCount++;
        }
        log.info("[onMessage][userId({}) 推送 FRIEND_INFO_UPDATED 给 {} 位好友]", userId, notifyCount);
    }

}
