package cn.iocoder.yudao.module.iot.service.rule.data.action.websocket;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkWebSocketConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * IoT WebSocket 客户端
 * <p>
 * 负责与外部 WebSocket 服务器建立连接并发送设备消息
 * 支持 ws:// 和 wss:// 协议，支持 JSON 和 TEXT 数据格式
 * 基于 OkHttp WebSocket 实现，兼容 JDK 8+
 * <p>
 * 注意：该类的线程安全由调用方（IotWebSocketDataRuleAction）通过分布式锁保证
 *
 * @author HUIHUI
 */
@Slf4j
public class IotWebSocketClient {

    private final String serverUrl;
    private final Integer connectTimeoutMs;
    private final Integer sendTimeoutMs;
    private final String dataFormat;

    private OkHttpClient okHttpClient;
    private volatile WebSocket webSocket;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public IotWebSocketClient(String serverUrl, Integer connectTimeoutMs, Integer sendTimeoutMs, String dataFormat) {
        this.serverUrl = serverUrl;
        this.connectTimeoutMs = connectTimeoutMs != null ? connectTimeoutMs : IotDataSinkWebSocketConfig.DEFAULT_CONNECT_TIMEOUT_MS;
        this.sendTimeoutMs = sendTimeoutMs != null ? sendTimeoutMs : IotDataSinkWebSocketConfig.DEFAULT_SEND_TIMEOUT_MS;
        this.dataFormat = dataFormat != null ? dataFormat : IotDataSinkWebSocketConfig.DEFAULT_DATA_FORMAT;
    }

    /**
     * 连接到 WebSocket 服务器
     * <p>
     * 注意：调用方需要通过分布式锁保证并发安全
     */
    public void connect() throws Exception {
        if (connected.get()) {
            log.warn("[connect][WebSocket 客户端已经连接，无需重复连接]");
            return;
        }

        try {
            // 创建 OkHttpClient
            okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
                    .readTimeout(sendTimeoutMs, TimeUnit.MILLISECONDS)
                    .writeTimeout(sendTimeoutMs, TimeUnit.MILLISECONDS)
                    .build();

            // 创建 WebSocket 请求
            Request request = new Request.Builder()
                    .url(serverUrl)
                    .build();

            // 使用 CountDownLatch 等待连接完成
            CountDownLatch connectLatch = new CountDownLatch(1);
            AtomicBoolean connectSuccess = new AtomicBoolean(false);
            // 创建 WebSocket 连接
            webSocket = okHttpClient.newWebSocket(request, new IotWebSocketListener(connectLatch, connectSuccess));

            // 等待连接完成
            boolean await = connectLatch.await(connectTimeoutMs, TimeUnit.MILLISECONDS);
            if (!await || !connectSuccess.get()) {
                close();
                throw new Exception("WebSocket 连接超时或失败，服务器地址: " + serverUrl);
            }
            log.info("[connect][WebSocket 客户端连接成功，服务器地址: {}]", serverUrl);
        } catch (Exception e) {
            close();
            log.error("[connect][WebSocket 客户端连接失败，服务器地址: {}]", serverUrl, e);
            throw e;
        }
    }

    /**
     * 发送设备消息
     *
     * @param message 设备消息
     * @throws Exception 发送异常
     */
    public void sendMessage(IotDeviceMessage message) throws Exception {
        WebSocket ws = this.webSocket;
        if (!connected.get() || ws == null) {
            throw new IllegalStateException("WebSocket 客户端未连接");
        }

        try {
            String messageData;
            if (IotDataSinkWebSocketConfig.DEFAULT_DATA_FORMAT.equalsIgnoreCase(dataFormat)) {
                messageData = JsonUtils.toJsonString(message);
            } else {
                messageData = message.toString();
            }

            // 发送消息
            boolean success = ws.send(messageData);
            if (!success) {
                throw new Exception("WebSocket 发送消息失败，消息队列已满或连接已关闭");
            }
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
        try {
            if (webSocket != null) {
                // 发送正常关闭帧，状态码 1000 表示正常关闭
                // TODO @puhui999：有没 1000 的枚举哈？在 okhttp 里
                webSocket.close(1000, "客户端主动关闭");
                webSocket = null;
            }
            if (okHttpClient != null) {
                // 关闭连接池和调度器
                okHttpClient.dispatcher().executorService().shutdown();
                okHttpClient.connectionPool().evictAll();
                okHttpClient = null;
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

    /**
     * OkHttp WebSocket 监听器
     */
    @SuppressWarnings("NullableProblems")
    private class IotWebSocketListener extends WebSocketListener {

        private final CountDownLatch connectLatch;
        private final AtomicBoolean connectSuccess;

        public IotWebSocketListener(CountDownLatch connectLatch, AtomicBoolean connectSuccess) {
            this.connectLatch = connectLatch;
            this.connectSuccess = connectSuccess;
        }

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            connected.set(true);
            connectSuccess.set(true);
            connectLatch.countDown();
            log.info("[onOpen][WebSocket 连接已打开，服务器: {}]", serverUrl);
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            log.debug("[onMessage][收到消息: {}]", text);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            connected.set(false);
            log.info("[onClosing][WebSocket 正在关闭，code: {}, reason: {}]", code, reason);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            connected.set(false);
            log.info("[onClosed][WebSocket 已关闭，code: {}, reason: {}]", code, reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            connected.set(false);
            connectLatch.countDown(); // 确保连接失败时也释放等待
            log.error("[onFailure][WebSocket 连接失败]", t);
        }
    }

}
