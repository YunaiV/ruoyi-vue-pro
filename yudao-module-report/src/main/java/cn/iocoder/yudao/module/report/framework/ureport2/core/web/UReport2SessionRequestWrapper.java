package cn.iocoder.yudao.module.report.framework.ureport2.core.web;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import java.util.Enumeration;
import java.util.Objects;

/**
 * 为 UReport2 提供稳定 Session 编号的 {@link HttpServletRequest} 包装类
 *
 * UReport2 的未保存预览（TempObjectCache）、图表数据（HttpSessionReportCache）以 getSession().getId() 作为缓存 key。
 * 但 Spring Security 的 SessionManagementFilter 在每次 token 认证后，都会调用 changeSessionId() 防护会话固定，
 * 导致物理 Session 编号每个请求都变化，UReport2 读取不到上一个请求写入的缓存。
 *
 * 因此，返回的 Session 的 getId() 替换为保存在真实 Session 属性中的随机编号：
 * 1. 物理 Session 编号轮换时，属性随 Session 保留，编号保持不变
 * 2. 同一个 Session 内登录用户或租户变化时，重新生成编号，不会读取到其它身份的缓存
 *
 * 其它方法均委托给真实 Session，物理 Session 仍由容器与 Spring Security 管理
 *
 * @author 芋道源码
 */
public class UReport2SessionRequestWrapper extends HttpServletRequestWrapper {

    static final String IDENTITY_ATTRIBUTE = UReport2SessionRequestWrapper.class.getName() + ".IDENTITY";
    static final String ID_ATTRIBUTE = UReport2SessionRequestWrapper.class.getName() + ".ID";

    /**
     * 登录身份，见 {@link #buildIdentity(LoginUser)}
     */
    private final String identity;

    public UReport2SessionRequestWrapper(HttpServletRequest request, LoginUser loginUser) {
        super(request);
        this.identity = buildIdentity(loginUser);
    }

    /**
     * 生成登录身份，由用户类型、用户编号、租户编号组成；身份不同，UReport2 看到的 Session 编号也不同
     *
     * @param loginUser 登录用户
     * @return 登录身份
     */
    static String buildIdentity(LoginUser loginUser) {
        return loginUser.getUserType() + ":" + loginUser.getId() + ":" + loginUser.getTenantId();
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    @Override
    public HttpSession getSession(boolean create) {
        HttpSession session = super.getSession(create);
        if (session == null) {
            return null;
        }
        return new UReport2HttpSession(session, getOrCreateId(session));
    }

    /**
     * 获得 UReport2 看到的 Session 编号，不存在时创建
     *
     * 编号保存在真实 Session 的属性中，所以物理 Session 编号轮换后保持不变；
     * 如果 Session 中记录的登录身份与当前不一致（例如说切换了用户、租户），则重新生成，避免读取到其它身份的缓存。
     * 通过 Session 互斥锁，保证同一个 Session 的并发请求拿到同一个编号
     *
     * @param session 真实 Session
     * @return Session 编号
     */
    private String getOrCreateId(HttpSession session) {
        synchronized (WebUtils.getSessionMutex(session)) {
            String id = (String) session.getAttribute(ID_ATTRIBUTE);
            if (id != null && Objects.equals(identity, session.getAttribute(IDENTITY_ATTRIBUTE))) {
                return id;
            }
            // 首次访问，或者登录身份变化：重新生成编号
            id = IdUtil.fastSimpleUUID();
            session.setAttribute(IDENTITY_ATTRIBUTE, identity);
            session.setAttribute(ID_ATTRIBUTE, id);
            return id;
        }
    }

    /**
     * 替换 getId() 的 {@link HttpSession} 实现类，其它方法委托给真实 Session
     *
     * @author 芋道源码
     */
    static class UReport2HttpSession implements HttpSession {

        private final HttpSession session;
        private final String id;

        UReport2HttpSession(HttpSession session, String id) {
            this.session = session;
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public long getCreationTime() {
            return session.getCreationTime();
        }

        @Override
        public long getLastAccessedTime() {
            return session.getLastAccessedTime();
        }

        @Override
        public ServletContext getServletContext() {
            return session.getServletContext();
        }

        @Override
        public void setMaxInactiveInterval(int interval) {
            session.setMaxInactiveInterval(interval);
        }

        @Override
        public int getMaxInactiveInterval() {
            return session.getMaxInactiveInterval();
        }

        @Override
        public Object getAttribute(String name) {
            return session.getAttribute(name);
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return session.getAttributeNames();
        }

        @Override
        public void setAttribute(String name, Object value) {
            session.setAttribute(name, value);
        }

        @Override
        public void removeAttribute(String name) {
            session.removeAttribute(name);
        }

        @Override
        public void invalidate() {
            session.invalidate();
        }

        @Override
        public boolean isNew() {
            return session.isNew();
        }

        // ========== Servlet 4.0 已废弃的方法，同样委托给真实 Session ==========

        @Override
        @Deprecated
        public HttpSessionContext getSessionContext() {
            return session.getSessionContext();
        }

        @Override
        @Deprecated
        public Object getValue(String name) {
            return session.getValue(name);
        }

        @Override
        @Deprecated
        public String[] getValueNames() {
            return session.getValueNames();
        }

        @Override
        @Deprecated
        public void putValue(String name, Object value) {
            session.putValue(name, value);
        }

        @Override
        @Deprecated
        public void removeValue(String name) {
            session.removeValue(name);
        }

    }

}
