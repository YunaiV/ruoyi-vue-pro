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
    public void sendPrivateMessageAsync(Collection<Long> userIds, ImPrivateMessageDTO dto) {
        // 说明：通过 executeAfterTransaction 保证事务提交后再推送，避免客户端收到消息后查询数据库时事务尚未提交
        // 通过 getSelf() 获取 Spring 代理对象调用 @Async 方法，确保异步 AOP 生效（直接 this 调用会绕过代理）
        executeAfterTransaction(() -> getSelf().doSendPrivateMessage(userIds, dto));
    }

    @Override
    public void sendGroupMessageAsync(Collection<Long> userIds, ImGroupMessageDTO dto) {
        executeAfterTransaction(() -> getSelf().doSendGroupMessage(userIds, dto));
    }

    @Override
    public void sendChannelMessageAsync(Collection<Long> userIds, ImChannelMessageDTO dto) {
        executeAfterTransaction(() -> getSelf().doSendChannelMessage(userIds, dto));
    }

    @Override
    public void broadcastChannelMessageAsync(ImChannelMessageDTO dto) {
        executeAfterTransaction(() -> getSelf().doBroadcastChannelMessage(dto));
    }

    /**
     * 异步发送私聊 WebSocket 消息；多收件人共享同一 dto，避免按收件人重复注册 afterCommit 回调
     */
    @Async
    public void doSendPrivateMessage(Collection<Long> userIds, ImPrivateMessageDTO dto) {
        for (Long userId : getDistinctUserIds(userIds)) {
            try {
                webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), userId,
                        ImPrivateMessageDTO.TYPE, dto);
            } catch (Exception e) {
                log.error("[doSendPrivateMessage][userId({}) dto({}) 发送失败]", userId, dto, e);
            }
        }
    }

    /**
     * 异步发送群聊 WebSocket 消息
     */
    @Async
    public void doSendGroupMessage(Collection<Long> userIds, ImGroupMessageDTO dto) {
        for (Long userId : getDistinctUserIds(userIds)) {
            try {
                webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), userId,
                        ImGroupMessageDTO.TYPE, dto);
            } catch (Exception e) {
                log.error("[doSendGroupMessage][userId({}) dto({}) 发送失败]", userId, dto, e);
            }
        }
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
