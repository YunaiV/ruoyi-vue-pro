package cn.iocoder.yudao.framework.websocket.core.session;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

/**
 * {@link WebSocketHandler} 的装饰类，实现了以下功能：
 *
 * 1. {@link WebSocketSession} 连接或关闭时，使用 {@link #sessionManager} 进行管理
 * 2. 封装 {@link WebSocketSession} 支持并发操作
 *
 * @author 芋道源码
 */
public class WebSocketSessionHandlerDecorator extends WebSocketHandlerDecorator {

    /**
     * 发送时间的限制，单位：毫秒
     */
    private static final Integer SEND_TIME_LIMIT = 1000 * 5;
    /**
     * 发送消息缓冲上线，单位：bytes
     */
    private static final Integer BUFFER_SIZE_LIMIT = 1024 * 100;

    private final WebSocketSessionManager sessionManager;

    public WebSocketSessionHandlerDecorator(WebSocketHandler delegate,
                                            WebSocketSessionManager sessionManager) {
        super(delegate);
        this.sessionManager = sessionManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 实现 session 支持并发，可参考 https://blog.csdn.net/abu935009066/article/details/131218149
        session = new ConcurrentWebSocketSessionDecorator(session, SEND_TIME_LIMIT, BUFFER_SIZE_LIMIT);
        // 添加到 WebSocketSessionManager 中
        sessionManager.addSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        sessionManager.removeSession(session);
    }

}
