package cn.iocoder.yudao.module.iot.service.rule.data.action.websocket;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkWebSocketConfig;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * IoT WebSocket 客户端
 * <p>
 * 负责与外部 WebSocket 服务器建立连接并发送设备消息
 * 支持 ws:// 和 wss:// 协议，支持 JSON 和 TEXT 数据格式
 * 基于 Java 11+ 内置的 java.net.http.WebSocket 实现
 *
 * @author HUIHUI
 */
@Slf4j
public class IotWebSocketClient implements WebSocket.Listener {

    private final String serverUrl;
    private final Integer connectTimeoutMs;
    private final Integer sendTimeoutMs;
    private final String dataFormat;

    private WebSocket webSocket;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final StringBuilder messageBuffer = new StringBuilder();

    public IotWebSocketClient(String serverUrl, Integer connectTimeoutMs, Integer sendTimeoutMs, String dataFormat) {
        this.serverUrl = serverUrl;
        this.connectTimeoutMs = connectTimeoutMs != null ? connectTimeoutMs : IotDataSinkWebSocketConfig.DEFAULT_CONNECT_TIMEOUT_MS;
        this.sendTimeoutMs = sendTimeoutMs != null ? sendTimeoutMs : IotDataSinkWebSocketConfig.DEFAULT_SEND_TIMEOUT_MS;
        this.dataFormat = dataFormat != null ? dataFormat : IotDataSinkWebSocketConfig.DEFAULT_DATA_FORMAT;
    }

    /**
     * 连接到 WebSocket 服务器
     */
    @SuppressWarnings("resource")
    public void connect() throws Exception {
        if (connected.get()) {
            log.warn("[connect][WebSocket 客户端已经连接，无需重复连接]");
            return;
        }

        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                    .build();

            CompletableFuture<WebSocket> future = httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                    .buildAsync(URI.create(serverUrl), this);

            // 等待连接完成
            webSocket = future.get(connectTimeoutMs, TimeUnit.MILLISECONDS);
            connected.set(true);
            log.info("[connect][WebSocket 客户端连接成功，服务器地址: {}]", serverUrl);
        } catch (Exception e) {
            close();
            log.error("[connect][WebSocket 客户端连接失败，服务器地址: {}]", serverUrl, e);
            throw e;
        }
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        log.debug("[onOpen][WebSocket 连接已打开]");
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        messageBuffer.append(data);
        if (last) {
            log.debug("[onText][收到 WebSocket 消息: {}]", messageBuffer);
            messageBuffer.setLength(0);
        }
        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        connected.set(false);
        log.info("[onClose][WebSocket 连接已关闭，状态码: {}，原因: {}]", statusCode, reason);
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        connected.set(false);
        log.error("[onError][WebSocket 发生错误]", error);
    }

    /**
     * 发送设备消息
     *
     * @param message 设备消息
     * @throws Exception 发送异常
     */
    public void sendMessage(IotDeviceMessage message) throws Exception {
        if (!connected.get() || webSocket == null) {
            throw new IllegalStateException("WebSocket 客户端未连接");
        }

        try {
            String messageData;
            if (IotDataSinkWebSocketConfig.DEFAULT_DATA_FORMAT.equalsIgnoreCase(dataFormat)) {
                messageData = JsonUtils.toJsonString(message);
            } else {
                messageData = message.toString();
            }

            // 发送消息并等待完成
            CompletableFuture<WebSocket> future = webSocket.sendText(messageData, true);
            future.get(sendTimeoutMs, TimeUnit.MILLISECONDS);
            log.debug("[sendMessage][发送消息成功，设备 ID: {}，消息长度: {}]",
                    message.getDeviceId(), messageData.length());
        } catch (Exception e) {
            log.error("[sendMessage][发送消息失败，设备 ID: {}]", message.getDeviceId(), e);
            throw e;
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (!connected.get() && webSocket == null) {
            return;
        }

        try {
            if (webSocket != null) {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "客户端主动关闭")
                        .orTimeout(5, TimeUnit.SECONDS)
                        .exceptionally(e -> {
                            log.warn("[close][发送关闭帧失败]", e);
                            return null;
                        });
            }
            connected.set(false);
            log.info("[close][WebSocket 客户端连接已关闭，服务器地址: {}]", serverUrl);
        } catch (Exception e) {
            log.error("[close][关闭 WebSocket 客户端连接异常]", e);
        }
    }

    /**
     * 检查连接状态
     *
     * @return 是否已连接
     */
    public boolean isConnected() {
        return connected.get() && webSocket != null;
    }

    @Override
    public String toString() {
        return "IotWebSocketClient{" +
                "serverUrl='" + serverUrl + '\'' +
                ", dataFormat='" + dataFormat + '\'' +
                ", connected=" + connected.get() +
                '}';
    }

}
