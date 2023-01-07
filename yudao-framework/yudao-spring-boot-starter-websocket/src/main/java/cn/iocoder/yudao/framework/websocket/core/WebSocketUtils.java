package cn.iocoder.yudao.framework.websocket.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
public class WebSocketUtils {
    public static boolean sendMessage(WebSocketSession seesion, String message) {
        if (seesion == null) {
            log.error("seesion 不存在");
            return false;
        }
        if (seesion.isOpen()) {
            try {
                seesion.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("WebSocket 消息发送异常 Session={} | msg= {} | exception={}", seesion, message, e);
                return false;
            }
        }
        return true;
    }

    public static boolean sendMessage(Object sessionKey, String message) {
        WebSocketSession session = WebSocketSessionHandler.getSession(sessionKey);
        return sendMessage(session, message);
    }
}
