package cn.iocoder.yudao.framework.websocket.core.util;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/**
 * 专属于 web 包的工具类
 *
 * @author 芋道源码
 */
public class WebSocketFrameworkUtils {

    public static final String ATTRIBUTE_LOGIN_USER = "LOGIN_USER";

    /**
     * 设置当前用户
     *
     * @param loginUser 登录用户
     * @param attributes Session
     */
    public static void setLoginUser(LoginUser loginUser, Map<String, Object> attributes) {
        attributes.put(ATTRIBUTE_LOGIN_USER, loginUser);
    }

    /**
     * 获取当前用户
     *
     * @return 当前用户
     */
    public static LoginUser getLoginUser(WebSocketSession session) {
        return (LoginUser) session.getAttributes().get(ATTRIBUTE_LOGIN_USER);
    }

    /**
     * 获得当前用户的编号
     *
     * @return 用户编号
     */
    @Nullable
    public static Long getLoginUserId(WebSocketSession session) {
        LoginUser loginUser = getLoginUser(session);
        return loginUser != null ? loginUser.getId() : null;
    }

    /**
     * 获得当前用户的类型
     *
     * @return 用户编号
     */
    @Nullable
    public static Integer getLoginUserType(WebSocketSession session) {
        LoginUser loginUser = getLoginUser(session);
        return loginUser != null ? loginUser.getUserType() : null;
    }

}
