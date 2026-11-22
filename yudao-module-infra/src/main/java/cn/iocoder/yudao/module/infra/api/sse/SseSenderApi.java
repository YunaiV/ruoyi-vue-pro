package cn.iocoder.yudao.module.infra.api.sse;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;

/**
 * SSE 发送器的 API 接口
 *
 * 对 SseMessageSender 进行封装，提供给其它模块使用
 *
 * @author 芋道源码
 */
public interface SseSenderApi {

    /**
     * 发送消息给指定用户
     *
     * @param userType 用户类型
     * @param userId 用户编号
     * @param messageType 消息类型，作为 SSE 的 event 名
     * @param messageContent 消息内容，JSON 格式
     */
    void send(Integer userType, Long userId, String messageType, String messageContent);

    /**
     * 发送消息给指定用户类型
     *
     * @param userType 用户类型
     * @param messageType 消息类型，作为 SSE 的 event 名
     * @param messageContent 消息内容，JSON 格式
     */
    void send(Integer userType, String messageType, String messageContent);

    /**
     * 发送消息给指定 SSE 连接
     *
     * @param connectionId SSE 连接编号，以 sse: 开头
     * @param messageType 消息类型，作为 SSE 的 event 名
     * @param messageContent 消息内容，JSON 格式
     */
    void send(String connectionId, String messageType, String messageContent);

    default void sendObject(Integer userType, Long userId, String messageType, Object messageContent) {
        send(userType, userId, messageType, JsonUtils.toJsonString(messageContent));
    }

    default void sendObject(Integer userType, String messageType, Object messageContent) {
        send(userType, messageType, JsonUtils.toJsonString(messageContent));
    }

    default void sendObject(String connectionId, String messageType, Object messageContent) {
        send(connectionId, messageType, JsonUtils.toJsonString(messageContent));
    }

}
