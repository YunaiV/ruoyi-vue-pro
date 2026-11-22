package cn.iocoder.yudao.framework.websocket.core.util;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/**
 * 专属于 web 包的工具类
 *
 * @author 芋道源码
 */
public class WebSocketFrameworkUtils {

    public static final String ATTRIBUTE_LOGIN_USER = "LOGIN_USER";
    public static final String ATTRIBUTE_CLIENT_ID = "CLIENT_ID";

    /**
     * 客户端编号的请求参数
     */
    public static final String PARAMETER_CLIENT_ID = "clientId";

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
    public static Long getLoginUserId(WebSocketSession session) {
        LoginUser loginUser = getLoginUser(session);
        return loginUser != null ? loginUser.getId() : null;
    }

    /**
     * 获得当前用户的类型
     *
     * @return 用户编号
     */
    public static Integer getLoginUserType(WebSocketSession session) {
        LoginUser loginUser = getLoginUser(session);
        return loginUser != null ? loginUser.getUserType() : null;
    }

    /**
     * 获得当前用户的租户编号
     *
     * @param session Session
     * @return 租户编号
     */
    public static Long getTenantId(WebSocketSession session) {
        LoginUser loginUser = getLoginUser(session);
        return loginUser != null ? loginUser.getTenantId() : null;
    }

    /**
     * 设置客户端编号
     *
     * @param clientId 客户端编号
     * @param attributes Session
     */
    public static void setClientId(String clientId, Map<String, Object> attributes) {
        if (clientId != null) {
            attributes.put(ATTRIBUTE_CLIENT_ID, clientId);
        }
    }

    /**
     * 获得客户端编号
     *
     * @param session Session
     * @return 客户端编号
     */
    public static String getClientId(WebSocketSession session) {
        return (String) session.getAttributes().get(ATTRIBUTE_CLIENT_ID);
    }

    /**
     * 判断客户端编号是否合法：1-64 位字母、数字、下划线、中划线
     *
     * @param clientId 客户端编号
     * @return 是否合法
     */
    public static boolean isValidClientId(String clientId) {
        return clientId != null && clientId.matches("^[A-Za-z0-9_-]{1,64}$");
    }

}
