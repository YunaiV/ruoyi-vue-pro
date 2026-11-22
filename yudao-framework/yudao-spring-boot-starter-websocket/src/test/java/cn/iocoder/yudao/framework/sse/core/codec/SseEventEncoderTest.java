package cn.iocoder.yudao.framework.sse.core.codec;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.sse.core.message.SseMessage;
import cn.iocoder.yudao.framework.sse.core.session.RecordingSseEmitter;
import cn.iocoder.yudao.framework.sse.core.session.SseCloseReasonEnum;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link SseEventEncoder} 的单元测试
 *
 * @author 芋道源码
 */
public class SseEventEncoderTest {

    @Test
    public void testEncodeMessage_success() {
        // 准备参数
        SseMessage message = new SseMessage().setEventId("E1").setMessageType("notify-message-receive")
                .setMessageContent("{\"id\":1024,\"title\":\"审批通过\"}").setTimestamp(1795334400000L);

        // 调用
        String frame = RecordingSseEmitter.render(SseEventEncoder.encodeMessage(message));
        // 断言：event + id + data，结束于空行
        assertTrue(frame.startsWith("event:notify-message-receive\nid:E1\ndata:"), frame);
        assertTrue(frame.endsWith("\n\n"));
        Map<String, Object> json = JsonUtils.parseMap(frame.substring(frame.indexOf("data:") + 5, frame.length() - 2));
        assertEquals("notify-message-receive", json.get("type"));
        assertEquals("{\"id\":1024,\"title\":\"审批通过\"}", json.get("content"));
        assertEquals("E1", json.get("eventId"));
        assertEquals(1795334400000L, ((Number) json.get("timestamp")).longValue());
    }

    @Test
    public void testEncodeMessage_newLineInField() {
        // 准备参数：消息类型、eventId 中带换行
        SseMessage message = new SseMessage().setEventId("E1\nevent:hack").setMessageType("demo\r\ntype");

        // 调用
        String frame = RecordingSseEmitter.render(SseEventEncoder.encodeMessage(message));
        // 断言：换行被清理，不会注入额外的字段
        assertTrue(frame.contains("event:demotype\n"), frame);
        assertTrue(frame.contains("id:E1event:hack\n"), frame);
        assertFalse(frame.contains("\nevent:hack"), frame);
    }

    @Test
    public void testEncodeMessage_newLineInContent() {
        // 准备参数
        SseMessage message = new SseMessage().setEventId("E2").setMessageType("t")
                .setMessageContent("第一行\n第二行 <script>\"'&");

        // 调用
        String frame = RecordingSseEmitter.render(SseEventEncoder.encodeMessage(message));
        // 断言：JSON 序列化后换行被转义，只有一行 data
        assertEquals(1, Arrays.stream(frame.split("\\R")).filter(line -> line.startsWith("data:")).count(), frame);
        Map<String, Object> json = JsonUtils.parseMap(frame.substring(frame.indexOf("data:") + 5).trim());
        assertEquals("第一行\n第二行 <script>\"'&", json.get("content"));
    }

    @Test
    public void testEncodeMessage_empty() {
        // 调用
        String frame = RecordingSseEmitter.render(SseEventEncoder.encodeMessage(new SseMessage()));
        // 断言：使用默认事件名，没有 id
        assertTrue(frame.startsWith("event:message\n"), frame);
        assertFalse(frame.contains("id:"), frame);
    }

    @Test
    public void testEncodeConnected() {
        // 调用
        String frame = RecordingSseEmitter.render(SseEventEncoder.encodeConnected("sse:abc", 25000L, 5000L));
        // 断言
        assertTrue(frame.startsWith("retry:5000\nevent:connected\ndata:"), frame);
        Map<String, Object> json = JsonUtils.parseMap(frame.substring(frame.indexOf("data:") + 5).trim());
        assertEquals("sse:abc", json.get("connectionId"));
        assertEquals(25000, ((Number) json.get("heartbeatInterval")).intValue());
        assertNotNull(json.get("serverTime"));
    }

    @Test
    public void testEncodeClose() {
        for (SseCloseReasonEnum reason : SseCloseReasonEnum.values()) {
            // 调用
            String frame = RecordingSseEmitter.render(SseEventEncoder.encodeClose(reason));
            // 断言
            assertTrue(frame.startsWith("event:close\ndata:"), frame);
            Map<String, Object> json = JsonUtils.parseMap(frame.substring(frame.indexOf("data:") + 5).trim());
            assertEquals(reason.name(), json.get("reason"));
            assertEquals(reason.isReconnect(), json.get("reconnect"));
        }
    }

    @Test
    public void testEncodeHeartbeat() {
        // 调用
        String frame = RecordingSseEmitter.render(SseEventEncoder.encodeHeartbeat());
        // 断言：注释帧，没有 event、data 字段
        assertTrue(frame.matches(":ping \\d+\\n\\n"), frame);
    }

}
