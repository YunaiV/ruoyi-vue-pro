package cn.iocoder.yudao.framework.websocket.core;

import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class WebSocketSessionHandler {
    private WebSocketSessionHandler() {
    }

    private static final Map<String, WebSocketSession> SESSION_MAP = new ConcurrentHashMap<>();

    public static void addSession(Object sessionKey, WebSocketSession session) {
        SESSION_MAP.put(sessionKey.toString(), session);
    }

    public static void removeSession(Object sessionKey) {
        SESSION_MAP.remove(sessionKey.toString());
    }

    public static WebSocketSession getSession(Object sessionKey) {
        return SESSION_MAP.get(sessionKey.toString());
    }

    public static Collection<WebSocketSession> getSessions() {
        return SESSION_MAP.values();
    }

    public static Set<String> getSessionKeys() {
        return SESSION_MAP.keySet();
    }

}
