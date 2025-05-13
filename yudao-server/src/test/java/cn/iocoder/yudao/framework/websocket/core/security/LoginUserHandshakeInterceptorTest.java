package cn.iocoder.yudao.framework.websocket.core.security;


import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.server.YudaoServerApplication;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Disabled("请确保应用能正常启动以后再执行这个测试用例")
@SpringBootTest(classes = YudaoServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

        AtomicBoolean connected = new AtomicBoolean(false);
        AtomicBoolean disconnected = new AtomicBoolean(false);
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = HttpUrl.get("http://localhost:" + port + "/infra/ws?token=" + accessToken);
        Request request = new Request(url, "GET", new Headers.Builder().build(), null, new HashMap<>());
        WebSocket webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosed(webSocket, code, reason);
                disconnected.set(true);
                log.info("onClosed, {}, {}", code, reason);
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosing(webSocket, code, reason);
                log.info("onClosing, {}, {}", code, reason);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                log.info("onFailure, {}, {}", t, response);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                super.onMessage(webSocket, text);
                log.info("onMessage, {}", text);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                super.onMessage(webSocket, bytes);
            }

            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                super.onOpen(webSocket, response);
                log.info("onOpen, {}", response);
                connected.set(true);
            }
        });
        await().atMost(Duration.ofSeconds(10)).untilTrue(connected);
        webSocket.close(1000, "close");
        await().atMost(Duration.ofSeconds(10)).untilTrue(disconnected);
    }
}
