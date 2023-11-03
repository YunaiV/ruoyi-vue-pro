package cn.iocoder.yudao.module.infra.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.Semaphore;

/**
 * websocket 消息处理
 */
@Component
@ServerEndpoint("/websocket/message")
@Slf4j
public class WebSocketServer {

    /**
     * 默认最多允许同时在线用户数100
     */
    public static int socketMaxOnlineCount = 100;

    private static final Semaphore SOCKET_SEMAPHORE = new Semaphore(socketMaxOnlineCount);

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) throws Exception {
        // 尝试获取信号量
        boolean semaphoreFlag = SemaphoreUtils.tryAcquire(SOCKET_SEMAPHORE);
        if (!semaphoreFlag) {
            // 未获取到信号量
            log.error("当前在线人数超过限制数：{}", socketMaxOnlineCount);
            WebSocketUsers.sendMessage(session, "当前在线人数超过限制数：" + socketMaxOnlineCount);
            session.close();
        } else {
            String userId = WebSocketUsers.getParam("userId", session);
            if (userId != null) {
                // 添加用户
                WebSocketUsers.addSession(userId, session);
                log.info("用户【userId={}】建立连接，当前连接用户总数：{}", userId, WebSocketUsers.getUsers().size());
                WebSocketUsers.sendMessage(session, "接收内容：连接成功");
            } else {
                WebSocketUsers.sendMessage(session, "接收内容：连接失败");
            }
        }
    }

    /**
     * 连接关闭时处理
     */
    @OnClose
    public void onClose(Session session) {
        log.info("用户【sessionId={}】关闭连接！", session.getId());
        // 移除用户
        WebSocketUsers.removeSession(session);
        // 获取到信号量则需释放
        SemaphoreUtils.release(SOCKET_SEMAPHORE);
    }

    /**
     * 抛出异常时处理
     */
    @OnError
    public void onError(Session session, Throwable exception) throws Exception {
        if (session.isOpen()) {
            // 关闭连接
            session.close();
        }
        String sessionId = session.getId();
        log.info("用户【sessionId={}】连接异常！异常信息：{}", sessionId, exception);
        // 移出用户
        WebSocketUsers.removeSession(session);
        // 获取到信号量则需释放
        SemaphoreUtils.release(SOCKET_SEMAPHORE);
    }

    /**
     * 收到客户端消息时调用的方法
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        WebSocketUsers.sendMessage(session, "接收内容：" + message);
    }
}
