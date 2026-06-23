package cn.iocoder.yudao.module.im.service.websocket;

import java.util.Collection;
import java.util.Collections;

/**
 * IM WebSocket 推送 Service 接口
 * <p>
 * 统一封装 WebSocket 通知推送，事务内调用时提交后异步执行。
 *
 * @author 芋道源码
 */
public interface ImWebSocketService {

    /**
     * 异步推送 WebSocket 通知给指定用户
     *
     * @param userId           目标用户编号
     * @param conversationType 会话类型，参见 ImConversationTypeEnum 枚举类
     * @param contentType      内容类型，参见 ImContentTypeEnum 枚举类
     * @param payload          通知 payload
     */
    default void sendNotificationAsync(Long userId, Integer conversationType, Integer contentType, Object payload) {
        sendNotificationAsync(Collections.singleton(userId), conversationType, contentType, payload);
    }

    /**
     * 异步批量推送 WebSocket 通知给多个用户
     *
     * @param userIds          目标用户编号列表
     * @param conversationType 会话类型，参见 ImConversationTypeEnum 枚举类
     * @param contentType      内容类型，参见 ImContentTypeEnum 枚举类
     * @param payload          通知 payload
     */
    void sendNotificationAsync(Collection<Long> userIds, Integer conversationType, Integer contentType, Object payload);

    /**
     * 异步广播 WebSocket 通知给当前所有在线管理端用户
     *
     * @param conversationType 会话类型，参见 ImConversationTypeEnum 枚举类
     * @param contentType      内容类型，参见 ImContentTypeEnum 枚举类
     * @param payload          通知 payload
     */
    void broadcastNotificationAsync(Integer conversationType, Integer contentType, Object payload);

}
