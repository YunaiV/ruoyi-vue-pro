package cn.iocoder.yudao.module.im.service.websocket;

import cn.iocoder.yudao.module.im.service.websocket.dto.ImGroupMessageDTO;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;

import java.util.Collection;
import java.util.Collections;

/**
 * IM WebSocket 推送 Service 接口
 * <p>
 * 统一封装 WebSocket 消息推送，所有方法默认异步执行。
 *
 * @author 芋道源码
 */
public interface ImWebSocketService {

    /**
     * 异步推送私聊消息给指定用户
     *
     * @param userId 目标用户编号
     * @param dto    私聊消息 DTO
     */
    default void sendPrivateMessageAsync(Long userId, ImPrivateMessageDTO dto) {
        sendPrivateMessageAsync(Collections.singleton(userId), dto);
    }

    /**
     * 异步批量推送私聊消息给多个用户；用于一条 payload 扇出到多个收件人
     * <p>
     * 相比逐个 sendPrivateMessageAsync，仅注册一个 afterCommit 回调 + 一个 @Async 任务，大群参与者事件（1602 / 1603）下避免 N 次线程池调度
     *
     * @param userIds 目标用户编号列表
     * @param dto     私聊消息 DTO
     */
    void sendPrivateMessageAsync(Collection<Long> userIds, ImPrivateMessageDTO dto);

    /**
     * 异步推送群聊消息给指定用户
     *
     * @param userId 目标用户编号
     * @param dto    群聊消息 DTO
     */
    default void sendGroupMessageAsync(Long userId, ImGroupMessageDTO dto) {
        sendGroupMessageAsync(Collections.singleton(userId), dto);
    }

    /**
     * 异步批量推送群聊消息给多个用户
     *
     * @param userIds 目标用户编号列表
     * @param dto     群聊消息 DTO
     */
    void sendGroupMessageAsync(Collection<Long> userIds, ImGroupMessageDTO dto);

}
