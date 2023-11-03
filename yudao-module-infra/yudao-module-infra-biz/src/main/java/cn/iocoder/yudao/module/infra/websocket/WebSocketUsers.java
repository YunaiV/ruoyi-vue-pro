package cn.iocoder.yudao.module.infra.websocket;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Strings;

import javax.validation.constraints.NotNull;
import javax.websocket.Session;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * websocket 客户端用户
 */
@Slf4j
public class WebSocketUsers {

    /**
     * 用户集
     *  TODO 需要登录用户的session？
     */
    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * 存储用户
     *
     * @param userId  唯一键
     * @param session 用户信息
     */
    public static void addSession(String userId, Session session) {
        SESSION_MAP.put(userId, session);
    }

    /**
     * 移除用户
     *
     * @param session 用户信息
     * @return 移除结果
     */
    public static boolean removeSession(Session session) {
        String key = null;
        boolean flag = SESSION_MAP.containsValue(session);
        if (flag) {
            Set<Map.Entry<String, Session>> entries = SESSION_MAP.entrySet();
            for (Map.Entry<String, Session> entry : entries) {
                Session value = entry.getValue();
                if (value.equals(session)) {
                    key = entry.getKey();
                    break;
                }
            }
        } else {
            return true;
        }
        return removeSession(key);
    }

    /**
     * 移出用户
     *
     * @param userId 用户id
     */
    public static boolean removeSession(String userId) {
        log.info("用户【userId={}】退出", userId);
        Session remove = SESSION_MAP.remove(userId);
        if (remove != null) {
            boolean containsValue = SESSION_MAP.containsValue(remove);
            log.info("用户【userId={}】退出{}，当前连接用户总数：{}", userId, containsValue ? "失败" : "成功", SESSION_MAP.size());
            return containsValue;
        } else {
            return true;
        }
    }

    /**
     * 获取在线用户列表
     *
     * @return 返回用户集合
     */
    public static Map<String, Session> getUsers() {
        return SESSION_MAP;
    }

    /**
     * 向所有在线人发送消息
     *
     * @param message 消息内容
     */
    public static void sendMessageToAll(String message) {
        SESSION_MAP.forEach((userId, session) -> {
            if (session.isOpen()) {
                sendMessage(session, message);
            }
        });
    }

    /**
     * 异步发送文本消息
     *
     * @param session 用户session
     * @param message 消息内容
     */
    public static void sendMessageAsync(Session session, String message) {
        if (session.isOpen()) {
            // TODO 需要加synchronized锁（synchronized(session)）？单个session创建线程？
            session.getAsyncRemote().sendText(message);
        } else {
            log.warn("用户【session={}】不在线", session.getId());
        }
    }

    /**
     * 同步发送文本消息
     *
     * @param session 用户session
     * @param message 消息内容
     */
    public static void sendMessage(Session session, String message) {
        try {
            if (session.isOpen()) {
                // TODO 需要加synchronized锁（synchronized(session)）？单个session创建线程？
                session.getBasicRemote().sendText(message);
            } else {
                log.warn("用户【session={}】不在线", session.getId());
            }
        } catch (IOException e) {
            log.error("发送消息异常", e);
        }

    }

    /**
     * 根据用户id发送消息
     *
     * @param userId  用户id
     * @param message 消息内容
     */
    public static void sendMessage(String userId, String message) {
        Session session = SESSION_MAP.get(userId);
        //判断是否存在该用户的session，并且是否在线
        if (session == null || !session.isOpen()) {
            return;
        }
        sendMessage(session, message);
    }


    /**
     * 获取session中的指定参数值
     *
     * @param key     参数key
     * @param session 用户session
     */
    public static String getParam(@NotNull String key, Session session) {
        //TODO 目前只针对获取一个key的值，后期根据情况拓展多个 或者直接在onClose onOpen上获取参数？
        String value = null;
        Map<String, List<String>> parameters = session.getRequestParameterMap();
        if (MapUtil.isNotEmpty(parameters)) {
            value = parameters.get(key).get(0);
        } else {
            String queryString = session.getQueryString();
            if (!StrUtil.isEmpty(queryString)) {
                String[] params = Strings.split(queryString, '&');
                for (String paramPair : params) {
                    String[] nameValues = Strings.split(paramPair, '=');
                    if (key.equals(nameValues[0])) {
                        value = nameValues[1];
                    }
                }
            }
        }
        return value;
    }
}
