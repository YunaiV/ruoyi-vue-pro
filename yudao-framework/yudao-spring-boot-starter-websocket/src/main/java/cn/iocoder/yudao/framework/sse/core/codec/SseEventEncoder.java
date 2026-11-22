package cn.iocoder.yudao.framework.sse.core.codec;

import cn.iocoder.yudao.framework.sse.core.message.SseMessage;
import cn.iocoder.yudao.framework.sse.core.session.SseCloseReasonEnum;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SSE 事件帧编码器
 *
 * 1. 业务事件：id: {eventId}、event: {messageType}、data: {type, content, eventId, timestamp}，data 与 WebSocket 的 {type, content} 兼容
 * 2. 连接事件：retry: {retryInterval}、event: connected、data: {connectionId, heartbeatInterval, serverTime}
 * 3. 关闭事件：event: close、data: {reason, reconnect, message}
 * 4. 心跳：注释帧 : ping {timestamp}，不会触发前端的 onmessage
 *
 * @author 芋道源码
 */
public class SseEventEncoder {

    public static final String EVENT_CONNECTED = "connected";
    public static final String EVENT_CLOSE = "close";
    /**
     * 未指定消息类型时的事件名，与浏览器 EventSource 的默认事件一致
     */
    public static final String EVENT_DEFAULT = "message";

    /**
     * 编码业务事件
     *
     * @param message 消息
     * @return 事件
     */
    public static SseEmitter.SseEventBuilder encodeMessage(SseMessage message) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", message.getMessageType());
        data.put("content", message.getMessageContent());
        data.put("eventId", message.getEventId());
        data.put("timestamp", message.getTimestamp());
        String eventName = StrUtil.blankToDefault(sanitizeField(message.getMessageType()), EVENT_DEFAULT);
        SseEmitter.SseEventBuilder event = SseEmitter.event().name(eventName);
        String eventId = sanitizeField(message.getEventId());
        if (StrUtil.isNotEmpty(eventId)) {
            event.id(eventId);
        }
        return appendData(event, JsonUtils.toJsonString(data));
    }

    /**
     * 编码连接成功事件
     *
     * @param connectionId 连接编号
     * @param heartbeatIntervalMillis 心跳间隔，单位：毫秒
     * @param retryIntervalMillis 重连间隔，单位：毫秒
     * @return 事件
     */
    public static SseEmitter.SseEventBuilder encodeConnected(String connectionId,
                                                             long heartbeatIntervalMillis, long retryIntervalMillis) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("connectionId", connectionId);
        data.put("heartbeatInterval", heartbeatIntervalMillis);
        data.put("retryInterval", retryIntervalMillis);
        data.put("serverTime", System.currentTimeMillis());
        SseEmitter.SseEventBuilder event = SseEmitter.event().reconnectTime(retryIntervalMillis).name(EVENT_CONNECTED);
        return appendData(event, JsonUtils.toJsonString(data));
    }

    /**
     * 编码关闭事件
     *
     * @param reason 关闭原因
     * @return 事件
     */
    public static SseEmitter.SseEventBuilder encodeClose(SseCloseReasonEnum reason) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reason", reason.name());
        data.put("reconnect", reason.isReconnect());
        data.put("message", reason.getDescription());
        SseEmitter.SseEventBuilder event = SseEmitter.event().name(EVENT_CLOSE);
        return appendData(event, JsonUtils.toJsonString(data));
    }

    /**
     * 编码心跳注释帧
     *
     * @return 事件
     */
    public static SseEmitter.SseEventBuilder encodeHeartbeat() {
        return SseEmitter.event().comment("ping " + System.currentTimeMillis());
    }

    /**
     * 追加 data 字段：含换行时按行拆分为多个 data 字段，客户端会用 \n 重新拼接
     */
    private static SseEmitter.SseEventBuilder appendData(SseEmitter.SseEventBuilder event, String data) {
        String[] lines = StrUtil.nullToEmpty(data).split("\r\n|\r|\n", -1);
        for (String line : lines) {
            event.data(line);
        }
        return event;
    }

    /**
     * 清理 id、event 字段中的换行，避免注入额外的字段
     */
    private static String sanitizeField(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("[\r\n]", "");
    }

}
