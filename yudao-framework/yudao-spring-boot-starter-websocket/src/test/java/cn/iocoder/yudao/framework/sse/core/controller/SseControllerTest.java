package cn.iocoder.yudao.framework.sse.core.controller;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.sse.config.SseProperties;
import cn.iocoder.yudao.framework.sse.core.session.SseConnection;
import cn.iocoder.yudao.framework.sse.core.session.SseEmitterSessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link SseController} 的单元测试
 *
 * @author 芋道源码
 */
public class SseControllerTest {

    private SseEmitterSessionManager sessionManager;
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        sessionManager = new SseEmitterSessionManager(1000L);
        mockMvc = MockMvcBuilders.standaloneSetup(new SseController(sessionManager, new SseProperties()))
                .addPlaceholderValue("yudao.sse.path", "/infra/sse").build();
    }

    @AfterEach
    public void tearDown() {
        sessionManager.shutdown();
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testConnect_success() throws Exception {
        // mock 方法：登录用户
        login(1L, 1, 100L);

        // 调用：请求参数、tenant-id 请求头中伪造的用户类型与租户，应被忽略
        MvcResult result = mockMvc.perform(get("/infra/sse/connect")
                        .param("clientId", "tab-1").param("userType", "2").param("tenantId", "999")
                        .header("tenant-id", "999").header("Last-Event-ID", "E0"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();
        // 断言：响应头
        assertEquals("text/event-stream;charset=UTF-8", result.getResponse().getContentType());
        assertEquals("no-cache, no-transform", result.getResponse().getHeader("Cache-Control"));
        assertEquals("no", result.getResponse().getHeader("X-Accel-Buffering"));
        assertEquals("keep-alive", result.getResponse().getHeader("Connection"));
        // 断言：首帧为 connected
        String body = result.getResponse().getContentAsString();
        assertTrue(body.startsWith("retry:5000\nevent:connected\ndata:"), body);
        // 断言：连接的租户、用户类型取自 LoginUser
        SseConnection connection = sessionManager.getConnections().iterator().next();
        assertTrue(body.contains(connection.getConnectionId()));
        assertEquals(1L, connection.getTenantId());
        assertEquals(1, connection.getUserType());
        assertEquals(100L, connection.getUserId());
        assertEquals("tab-1", connection.getClientId());
        assertNotNull(connection.getExpiresTime());
    }

    @Test
    public void testConnect_notLogin() throws Exception {
        // 调用，并断言
        mockMvc.perform(get("/infra/sse/connect")).andExpect(status().isUnauthorized());
        assertEquals(0, sessionManager.getConnectionCount());
    }

    @Test
    public void testConnect_invalidClientId() throws Exception {
        // mock 方法：登录用户
        login(1L, 1, 100L);

        // 调用，并断言
        mockMvc.perform(get("/infra/sse/connect").param("clientId", "bad id\n"))
                .andExpect(status().isBadRequest());
        assertEquals(0, sessionManager.getConnectionCount());
    }

    @Test
    public void testConnect_shutdown() throws Exception {
        // mock 方法：登录用户，服务停机中
        login(1L, 1, 100L);
        sessionManager.shutdown();

        // 调用，并断言
        mockMvc.perform(get("/infra/sse/connect")).andExpect(status().isServiceUnavailable());
        assertEquals(0, sessionManager.getConnectionCount());
    }

    @Test
    public void testClose_success() throws Exception {
        // 准备参数
        login(1L, 1, 100L);
        mockMvc.perform(get("/infra/sse/connect")).andExpect(request().asyncStarted());
        SseConnection connection = sessionManager.getConnections().iterator().next();

        // 调用，并断言
        mockMvc.perform(post("/infra/sse/close").param("connectionId", connection.getConnectionId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
        assertNull(sessionManager.getConnection(connection.getConnectionId()));
    }

    @Test
    public void testClose_otherUser() throws Exception {
        // 准备参数
        login(1L, 1, 100L);
        mockMvc.perform(get("/infra/sse/connect")).andExpect(request().asyncStarted());
        SseConnection connection = sessionManager.getConnections().iterator().next();

        // 调用，并断言：同编号、不同租户的用户
        login(2L, 1, 100L);
        mockMvc.perform(post("/infra/sse/close").param("connectionId", connection.getConnectionId()))
                .andExpect(jsonPath("$.code").value(403));
        // 调用，并断言：同租户、不同用户
        login(1L, 1, 101L);
        mockMvc.perform(post("/infra/sse/close").param("connectionId", connection.getConnectionId()))
                .andExpect(jsonPath("$.code").value(403));
        assertNotNull(sessionManager.getConnection(connection.getConnectionId()));
    }

    @Test
    public void testClose_notExists() throws Exception {
        // mock 方法：登录用户
        login(1L, 1, 100L);

        // 调用，并断言
        mockMvc.perform(post("/infra/sse/close").param("connectionId", "sse:not-exists"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    public void testMaskToken() {
        // 调用，并断言：query 脱敏
        assertEquals("clientId=a&token=***", SseController.maskTokenQuery("clientId=a&token=abc123"));
        assertEquals("token=***&clientId=a", SseController.maskTokenQuery("token=abc123&clientId=a"));
        assertEquals("access_token=***", SseController.maskTokenQuery("access_token=abc"));
        assertEquals("mytoken=abc", SseController.maskTokenQuery("mytoken=abc"));

        // 准备参数
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/infra/sse/connect");
        request.setQueryString("token=secret&clientId=tab");
        // 调用，并断言：完整请求地址脱敏
        assertEquals("/infra/sse/connect?token=***&clientId=tab", SseController.maskToken(request));
    }

    private static void login(Long tenantId, Integer userType, Long userId) {
        LoginUser loginUser = new LoginUser().setTenantId(tenantId).setUserType(userType).setId(userId)
                .setExpiresTime(LocalDateTime.now().plusHours(1));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

}
