package cn.iocoder.yudao.framework.websocket.core.session;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.websocket.core.util.WebSocketFrameworkUtils;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 默认的 {@link WebSocketSessionManager} 实现类
 *
 * @author 芋道源码
 */
public class WebSocketSessionManagerImpl implements WebSocketSessionManager {

    /**
     * id 与 WebSocketSession 映射
     *
     * key：Session 编号
     */
    private final ConcurrentMap<String, WebSocketSession> idSessions = new ConcurrentHashMap<>();

    /**
     * user 与 WebSocketSession 映射
     *
     * key1：用户类型
     * key2：用户编号
     */
    private final ConcurrentMap<Integer, ConcurrentMap<Long, CopyOnWriteArrayList<WebSocketSession>>> userSessions
            = new ConcurrentHashMap<>();

    @Override
    public void addSession(WebSocketSession session) {
        // 添加到 idSessions 中
        idSessions.put(session.getId(), session);
        // 添加到 userSessions 中
        LoginUser user = WebSocketFrameworkUtils.getLoginUser(session);
        if (user == null) {
            return;
        }
        ConcurrentMap<Long, CopyOnWriteArrayList<WebSocketSession>> userSessionsMap = userSessions.get(user.getUserType());
        if (userSessionsMap == null) {
            userSessionsMap = new ConcurrentHashMap<>();
            if (userSessions.putIfAbsent(user.getUserType(), userSessionsMap) != null) {
                userSessionsMap = userSessions.get(user.getUserType());
            }
        }
        CopyOnWriteArrayList<WebSocketSession> sessions = userSessionsMap.get(user.getId());
        if (sessions == null) {
            sessions = new CopyOnWriteArrayList<>();
            if (userSessionsMap.putIfAbsent(user.getId(), sessions) != null) {
                sessions = userSessionsMap.get(user.getId());
            }
        }
        sessions.add(session);
    }

    @Override
    public void removeSession(WebSocketSession session) {
        // 移除从 idSessions 中
        idSessions.remove(session.getId());
        // 移除从 idSessions 中
        LoginUser user = WebSocketFrameworkUtils.getLoginUser(session);
        if (user == null) {
            return;
        }
        ConcurrentMap<Long, CopyOnWriteArrayList<WebSocketSession>> userSessionsMap = userSessions.get(user.getUserType());
        if (userSessionsMap == null) {
            return;
        }
        CopyOnWriteArrayList<WebSocketSession> sessions = userSessionsMap.get(user.getId());
        sessions.removeIf(session0 -> session0.getId().equals(session.getId()));
        if (CollUtil.isEmpty(sessions)) {
            userSessionsMap.remove(user.getId(), sessions);
        }
    }

    @Override
    public WebSocketSession getSession(String id) {
        return idSessions.get(id);
    }

    @Override
    public Collection<WebSocketSession> getSessionList(Integer userType) {
        ConcurrentMap<Long, CopyOnWriteArrayList<WebSocketSession>> userSessionsMap = userSessions.get(userType);
        if (CollUtil.isEmpty(userSessionsMap)) {
            return new ArrayList<>();
        }
        LinkedList<WebSocketSession> result = new LinkedList<>(); // 避免扩容
        Long contextTenantId = TenantContextHolder.getTenantId();
        for (List<WebSocketSession> sessions : userSessionsMap.values()) {
            if (CollUtil.isEmpty(sessions)) {
                continue;
            }
            // 特殊：如果租户不匹配，则直接排除
            if (contextTenantId != null) {
                Long userTenantId = WebSocketFrameworkUtils.getTenantId(sessions.get(0));
                if (!contextTenantId.equals(userTenantId)) {
                    continue;
                }
            }
            result.addAll(sessions);
        }
        return result;
    }

    @Override
    public Collection<WebSocketSession> getSessionList(Integer userType, Long userId) {
        ConcurrentMap<Long, CopyOnWriteArrayList<WebSocketSession>> userSessionsMap = userSessions.get(userType);
        if (CollUtil.isEmpty(userSessionsMap)) {
            return new ArrayList<>();
        }
        CopyOnWriteArrayList<WebSocketSession> sessions = userSessionsMap.get(userId);
        return CollUtil.isNotEmpty(sessions) ? new ArrayList<>(sessions) : new ArrayList<>();
    }

}
