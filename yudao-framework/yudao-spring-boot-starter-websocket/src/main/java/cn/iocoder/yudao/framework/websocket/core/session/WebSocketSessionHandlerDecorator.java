package cn.iocoder.yudao.framework.websocket.core.session;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.websocket.core.util.WebSocketFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import java.io.IOException;
import java.util.Objects;

/**
 * {@link WebSocketHandler} 的装饰类，实现了以下功能：
 *
 * 1. {@link WebSocketSession} 连接或关闭时，使用 {@link #sessionManager} 进行管理
 * 2. 封装 {@link WebSocketSession} 支持并发操作
 * 3. 同一用户、同一 clientId 重新连接时，关闭旧连接
 *
 * @author 芋道源码
 */
@Slf4j
public class WebSocketSessionHandlerDecorator extends WebSocketHandlerDecorator {

    /**
     * 发送时间的限制，单位：毫秒
     */
    private static final Integer SEND_TIME_LIMIT = 1000 * 5;
    /**
     * 发送消息缓冲上线，单位：bytes
     */
    private static final Integer BUFFER_SIZE_LIMIT = 1024 * 100;

    /**
     * 同一客户端重新连接，旧连接被替换时的关闭状态
     */
    public static final CloseStatus CLOSE_STATUS_REPLACED = new CloseStatus(4001, "REPLACED");

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
        // 同一 clientId 的旧连接，关闭替换
        closeReplacedSessions(session);
        // 添加到 WebSocketSessionManager 中
        sessionManager.addSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        sessionManager.removeSession(session);
    }

    private void closeReplacedSessions(WebSocketSession session) {
        String clientId = WebSocketFrameworkUtils.getClientId(session);
        LoginUser user = WebSocketFrameworkUtils.getLoginUser(session);
        if (clientId == null || user == null) {
            return;
        }
        for (WebSocketSession oldSession : sessionManager.getSessionList(user.getUserType(), user.getId())) {
            if (!clientId.equals(WebSocketFrameworkUtils.getClientId(oldSession))
                    || !Objects.equals(user.getTenantId(), WebSocketFrameworkUtils.getTenantId(oldSession))) {
                continue;
            }
            sessionManager.removeSession(oldSession);
            try {
                oldSession.close(CLOSE_STATUS_REPLACED);
            } catch (IOException ex) {
                log.warn("[closeReplacedSessions][session({}) 关闭失败]", oldSession.getId(), ex);
            }
        }
    }

}
