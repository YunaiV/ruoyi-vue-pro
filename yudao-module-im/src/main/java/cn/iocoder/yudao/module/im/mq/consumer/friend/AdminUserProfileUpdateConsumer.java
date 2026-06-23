package cn.iocoder.yudao.module.im.mq.consumer.friend;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.enums.ImContentTypeEnum;
import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.notification.friend.FriendInfoUpdatedNotification;
import cn.iocoder.yudao.module.system.api.message.user.AdminUserProfileUpdateMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Resource;
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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Async // Spring Event 默认在 Producer 发送的线程，通过 @Async 实现异步；事务提交后触发，避免回滚误推幽灵通知 / Consumer 抢在 commit 前读旧值
    public void onMessage(AdminUserProfileUpdateMessage message) {
        try {
            log.info("[onMessage][消息内容({})]", message);
            // 1. 过滤双向有效好友
            if (message == null || message.getUserId() == null) {
                return;
            }
            Long userId = message.getUserId();
            List<ImFriendDO> friends = friendService.getMutualEnableFriendList(userId);
            if (CollUtil.isEmpty(friends)) {
                return;
            }

            // 2. 给每个好友的多端推 FRIEND_INFO_UPDATED；payload 里 operatorUserId / friendUserId 都是「资料被改的人」
            int successCount = 0;
            for (ImFriendDO friend : friends) {
                try {
                    FriendInfoUpdatedNotification payload = (FriendInfoUpdatedNotification) new FriendInfoUpdatedNotification()
                            .setOperatorUserId(userId).setFriendUserId(userId);
                    websocketService.sendNotificationAsync(friend.getFriendUserId(),
                            ImConversationTypeEnum.NONE.getType(),
                            ImContentTypeEnum.FRIEND_INFO_UPDATED.getType(), payload);
                    successCount++;
                } catch (Exception e) {
                    log.warn("[onMessage][userId({}) friendUserId({}) 推送失败]",
                            userId, friend.getFriendUserId(), e);
                }
            }
            log.info("[onMessage][userId({}) 推送 FRIEND_INFO_UPDATED 给 {} 位好友]", userId, successCount);
        } catch (Exception e) {
            log.error("[onMessage][消息内容({}) 处理失败]", message, e);
        }
    }

}
