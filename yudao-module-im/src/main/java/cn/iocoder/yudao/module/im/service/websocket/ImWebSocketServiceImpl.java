package cn.iocoder.yudao.module.im.service.websocket;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.im.service.websocket.notification.ImNotificationWebSocketDTO;
import cn.iocoder.yudao.module.infra.api.websocket.WebSocketSenderApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertLinkedSet;

/**
 * IM WebSocket 推送 Service 实现类
 * <p>
 * 当调用方处于事务中时，推送会延迟到事务提交后再异步执行，
 * 避免客户端收到 WebSocket 消息时数据库变更尚未可见。
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ImWebSocketServiceImpl implements ImWebSocketService {

    @Resource
    private WebSocketSenderApi webSocketSenderApi;

    @Override
    public void sendNotificationAsync(Collection<Long> userIds, Integer conversationType, Integer contentType,
                                      Object payload) {
        ImNotificationWebSocketDTO notification = buildNotification(conversationType, contentType, payload);
        executeAfterTransaction(() -> getSelf().doSendNotification(userIds, notification));
    }

    @Override
    public void broadcastNotificationAsync(Integer conversationType, Integer contentType, Object payload) {
        ImNotificationWebSocketDTO notification = buildNotification(conversationType, contentType, payload);
        executeAfterTransaction(() -> getSelf().doBroadcastNotification(notification));
    }

    private static ImNotificationWebSocketDTO buildNotification(Integer conversationType, Integer contentType,
                                                                Object payload) {
        return new ImNotificationWebSocketDTO()
                .setConversationType(conversationType)
                .setContentType(contentType)
                .setPayload(payload);
    }

    /**
     * 异步发送 WebSocket 通知
     */
    @Async
    public void doSendNotification(Collection<Long> userIds, ImNotificationWebSocketDTO notification) {
        for (Long userId : getDistinctUserIds(userIds)) {
            try {
                webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), userId,
                        ImNotificationWebSocketDTO.TYPE, notification);
            } catch (Exception e) {
                log.error("[doSendNotification][userId({}) notification({}) 发送失败]", userId, notification, e);
            }
        }
    }

    /**
     * 异步广播 WebSocket 通知
     */
    @Async
    public void doBroadcastNotification(ImNotificationWebSocketDTO notification) {
        try {
            webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(),
                    ImNotificationWebSocketDTO.TYPE, notification);
        } catch (Exception e) {
            log.error("[doBroadcastNotification][notification({}) 广播失败]", notification, e);
        }
    }

    private static Set<Long> getDistinctUserIds(Collection<Long> userIds) {
        return convertLinkedSet(userIds, userId -> userId);
    }

    /**
     * 事务感知的任务调度
     *
     * @param task 待执行的推送任务
     */
    private void executeAfterTransaction(Runnable task) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                task.run();
            }

        });
    }

    /**
     * 获得自身的代理对象，解决 @Async AOP 代理问题
     */
    private ImWebSocketServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
