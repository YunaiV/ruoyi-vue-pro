package cn.iocoder.yudao.adminserver.modules.system.controller.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@Slf4j
@Controller
@ServerEndpoint("/ws/idwoklasdf")
public class WebSocketServerEndpoint {

    public WebSocketServerEndpoint() {
        log.info("WebSocketServerEndpoint()");
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        log.info("[onOpen][session({}) 接入]", session);
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        log.info("[onOpen][session({}) 接收到一条消息({})]", session, message); // 生产环境下，请设置成 debug 级别
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.info("[onClose][session({}) 连接关闭。关闭原因是({})}]", session, closeReason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.info("[onClose][session({}) 发生异常]", session, throwable);
    }
}
