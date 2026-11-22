package cn.iocoder.yudao.framework.websocket.core.security;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.websocket.core.util.WebSocketFrameworkUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * {@link LoginUserHandshakeInterceptor} 的单元测试
 *
 * @author 芋道源码
 */
public class LoginUserHandshakeInterceptorTest {

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static void login() {
        LoginUser loginUser = new LoginUser().setId(100L).setUserType(1).setTenantId(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    @Test
    public void testBeforeHandshake_notLogin() {
        // 准备参数
        Map<String, Object> attributes = new HashMap<>();

        // 调用
        boolean result = new LoginUserHandshakeInterceptor().beforeHandshake(
                new ServletServerHttpRequest(new MockHttpServletRequest()),
                new ServletServerHttpResponse(new MockHttpServletResponse()), mock(WebSocketHandler.class), attributes);
        // 断言
        assertFalse(result);
        assertTrue(attributes.isEmpty());
    }

    @Test
    public void testBeforeHandshake_success() {
        // mock 方法：登录用户
        login();
        // 准备参数
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("clientId", "tab-1");
        Map<String, Object> attributes = new HashMap<>();

        // 调用
        boolean result = new LoginUserHandshakeInterceptor().beforeHandshake(
                new ServletServerHttpRequest(request), new ServletServerHttpResponse(new MockHttpServletResponse()),
                mock(WebSocketHandler.class), attributes);
        // 断言
        assertTrue(result);
        assertEquals(100L, ((LoginUser) attributes.get(WebSocketFrameworkUtils.ATTRIBUTE_LOGIN_USER)).getId());
        assertEquals("tab-1", attributes.get(WebSocketFrameworkUtils.ATTRIBUTE_CLIENT_ID));
    }

    @Test
    public void testBeforeHandshake_invalidClientId() {
        // mock 方法：登录用户
        login();
        // 准备参数
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("clientId", "bad id");
        Map<String, Object> attributes = new HashMap<>();

        // 调用
        boolean result = new LoginUserHandshakeInterceptor().beforeHandshake(new ServletServerHttpRequest(request),
                new ServletServerHttpResponse(new MockHttpServletResponse()), mock(WebSocketHandler.class), attributes);
        // 断言：非法 clientId 被忽略，不影响握手
        assertTrue(result);
        assertNull(attributes.get(WebSocketFrameworkUtils.ATTRIBUTE_CLIENT_ID));
    }

}
