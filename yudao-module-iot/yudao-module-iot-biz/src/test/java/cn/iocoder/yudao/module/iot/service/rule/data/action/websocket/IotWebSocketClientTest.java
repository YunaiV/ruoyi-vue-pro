package cn.iocoder.yudao.module.iot.service.rule.data.action.websocket;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotWebSocketClient} 的单元测试
 *
 * @author HUIHUI
 */
class IotWebSocketClientTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    /**
     * 简单的 WebSocket 监听器，用于测试
     */
    private static class TestWebSocketListener extends WebSocketListener {
        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            // 连接打开
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            // 收到消息
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            webSocket.close(code, reason);
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            // 连接失败
        }
    }

    @Test
    public void testConstructor_defaultValues() {
        // 准备参数
        String serverUrl = "ws://localhost:8080";

        // 调用
        IotWebSocketClient client = new IotWebSocketClient(serverUrl, null, null, null);

        // 断言：验证默认值被正确设置
        assertNotNull(client);
        assertFalse(client.isConnected());
    }

    @Test
    public void testConstructor_customValues() {
        // 准备参数
        String serverUrl = "ws://localhost:8080";
        Integer connectTimeoutMs = 3000;
        Integer sendTimeoutMs = 5000;
        String dataFormat = "TEXT";

        // 调用
        IotWebSocketClient client = new IotWebSocketClient(serverUrl, connectTimeoutMs, sendTimeoutMs, dataFormat);

        // 断言
        assertNotNull(client);
        assertFalse(client.isConnected());
    }

    @Test
    public void testConnect_success() throws Exception {
        // 准备参数：使用 MockWebServer 的 WebSocket 端点
        String serverUrl = "ws://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort();
        IotWebSocketClient client = new IotWebSocketClient(serverUrl, 5000, 5000, "JSON");

        // mock：设置 MockWebServer 响应 WebSocket 升级请求
        mockWebServer.enqueue(new MockResponse().withWebSocketUpgrade(new TestWebSocketListener()));

        // 调用
        client.connect();

        // 断言
        assertTrue(client.isConnected());

        // 清理
        client.close();
    }

    @Test
    public void testConnect_alreadyConnected() throws Exception {
        // 准备参数
        String serverUrl = "ws://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort();
        IotWebSocketClient client = new IotWebSocketClient(serverUrl, 5000, 5000, "JSON");

        // mock
        mockWebServer.enqueue(new MockResponse().withWebSocketUpgrade(new TestWebSocketListener()));

        // 调用：第一次连接
        client.connect();
        assertTrue(client.isConnected());

        // 调用：第二次连接（应该不会重复连接）
        client.connect();
        assertTrue(client.isConnected());

        // 清理
        client.close();
    }

    @Test
    public void testSendMessage_success() throws Exception {
        // 准备参数
        String serverUrl = "ws://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort();
        IotWebSocketClient client = new IotWebSocketClient(serverUrl, 5000, 5000, "JSON");

        IotDeviceMessage message = IotDeviceMessage.builder()
                .deviceId(123L)
                .method("thing.property.report")
                .params("{\"temperature\": 25.5}")
                .build();

        // mock
        mockWebServer.enqueue(new MockResponse().withWebSocketUpgrade(new TestWebSocketListener()));

        // 调用
        client.connect();
        client.sendMessage(message);

        // 断言：消息发送成功不抛异常
        assertTrue(client.isConnected());

        // 清理
        client.close();
    }

    @Test
    public void testSendMessage_notConnected() {
        // 准备参数
        String serverUrl = "ws://localhost:8080";
        IotWebSocketClient client = new IotWebSocketClient(serverUrl, 5000, 5000, "JSON");

        IotDeviceMessage message = IotDeviceMessage.builder()
                .deviceId(123L)
                .method("thing.property.report")
                .params("{\"temperature\": 25.5}")
                .build();

        // 调用 & 断言：未连接时发送消息应抛出异常
        assertThrows(IllegalStateException.class, () -> client.sendMessage(message));
    }

    @Test
    public void testClose_success() throws Exception {
        // 准备参数
        String serverUrl = "ws://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort();
        IotWebSocketClient client = new IotWebSocketClient(serverUrl, 5000, 5000, "JSON");

        // mock
        mockWebServer.enqueue(new MockResponse().withWebSocketUpgrade(new TestWebSocketListener()));

        // 调用
        client.connect();
        assertTrue(client.isConnected());

        client.close();

        // 断言
        assertFalse(client.isConnected());
    }

    @Test
    public void testClose_notConnected() {
        // 准备参数
        String serverUrl = "ws://localhost:8080";
        IotWebSocketClient client = new IotWebSocketClient(serverUrl, 5000, 5000, "JSON");

        // 调用：关闭未连接的客户端不应抛异常
        assertDoesNotThrow(client::close);
        assertFalse(client.isConnected());
    }

    @Test
    public void testIsConnected_initialState() {
        // 准备参数
        String serverUrl = "ws://localhost:8080";
        IotWebSocketClient client = new IotWebSocketClient(serverUrl, 5000, 5000, "JSON");

        // 断言：初始状态应为未连接
        assertFalse(client.isConnected());
    }

    @Test
    public void testToString() {
        // 准备参数
        String serverUrl = "ws://localhost:8080";
        IotWebSocketClient client = new IotWebSocketClient(serverUrl, 5000, 5000, "JSON");

        // 调用
        String result = client.toString();

        // 断言
        assertNotNull(result);
        assertTrue(result.contains("serverUrl='ws://localhost:8080'"));
        assertTrue(result.contains("dataFormat='JSON'"));
        assertTrue(result.contains("connected=false"));
    }

    @Test
    public void testSendMessage_textFormat() throws Exception {
        // 准备参数
        String serverUrl = "ws://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort();
        IotWebSocketClient client = new IotWebSocketClient(serverUrl, 5000, 5000, "TEXT");

        IotDeviceMessage message = IotDeviceMessage.builder()
                .deviceId(123L)
                .method("thing.property.report")
                .params("{\"temperature\": 25.5}")
                .build();

        // mock
        mockWebServer.enqueue(new MockResponse().withWebSocketUpgrade(new TestWebSocketListener()));

        // 调用
        client.connect();
        client.sendMessage(message);

        // 断言：消息发送成功不抛异常
        assertTrue(client.isConnected());

        // 清理
        client.close();
    }

}
