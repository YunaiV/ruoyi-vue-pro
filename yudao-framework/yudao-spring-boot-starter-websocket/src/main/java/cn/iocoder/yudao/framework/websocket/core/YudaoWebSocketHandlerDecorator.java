package cn.iocoder.yudao.framework.websocket.core;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

public class YudaoWebSocketHandlerDecorator extends WebSocketHandlerDecorator {
    public YudaoWebSocketHandlerDecorator(WebSocketHandler delegate) {
        super(delegate);
    }

    /**
     * websocket 连接时执行的动作
     * @param session websocket session 对象
     * @throws Exception 异常对象
     */
    @Override
    public void afterConnectionEstablished(final WebSocketSession session) throws Exception {
        Object sessionKey = sessionKeyGen(session);
        WebSocketSessionHandler.addSession(sessionKey, session);
    }

    /**
     * websocket 关闭连接时执行的动作
     * @param session websocket session 对象
     * @param closeStatus 关闭状态对象
     * @throws Exception 异常对象
     */
    @Override
    public void afterConnectionClosed(final WebSocketSession session, CloseStatus closeStatus) throws Exception {
        Object sessionKey = sessionKeyGen(session);
        WebSocketSessionHandler.removeSession(sessionKey);
    }

    public Object sessionKeyGen(WebSocketSession webSocketSession) {

        Object obj = webSocketSession.getAttributes().get(WebSocketKeyDefine.LOGIN_USER);

        if (obj instanceof LoginUser) {
            LoginUser loginUser = (LoginUser) obj;
            // userId 作为唯一区分
            return String.valueOf(loginUser.getId());
        }

        return null;
    }
}
