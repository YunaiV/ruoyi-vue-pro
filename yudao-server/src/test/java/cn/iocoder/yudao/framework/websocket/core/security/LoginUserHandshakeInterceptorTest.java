package cn.iocoder.yudao.framework.websocket.core.security;


import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.server.YudaoServerApplication;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.LoggingWebSocketHandlerDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
//@Disabled("请确保应用能正常启动以后再执行这个测试用例")
@SpringBootTest(classes = YudaoServerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"yudao.tenant.enable=false" /* 禁用租户功能，简化测试用例 */})
@AutoConfigureMockMvc
public class LoginUserHandshakeInterceptorTest {

    @LocalServerPort
    private Integer port;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createWsConnection() throws Exception {
        String content = mockMvc.perform(post("/admin-api/system/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new JSONObject()
                                .put("username", "admin")
                                .put("password", "admin123")
                                .put("userType", String.valueOf(UserTypeEnum.ADMIN.getValue()))
                                .toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JSONObject res = new JSONObject(content);
        String accessToken = res.getJSONObject("data").getString("accessToken");

        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        LoggingWebSocketHandlerDecorator webSocketHandler = new LoggingWebSocketHandlerDecorator(new TextWebSocketHandler());
        String uri = "ws://localhost:" + port + "/infra/ws?token=" + accessToken;
        CompletableFuture<WebSocketSession> future = webSocketClient.execute(webSocketHandler, uri);
        WebSocketSession session = future.get(10, TimeUnit.SECONDS);
        assertTrue(session.isOpen());
        session.close();
    }
}
