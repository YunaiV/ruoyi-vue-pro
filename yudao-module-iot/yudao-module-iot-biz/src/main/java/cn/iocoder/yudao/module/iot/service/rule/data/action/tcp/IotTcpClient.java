package cn.iocoder.yudao.module.iot.service.rule.data.action.tcp;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * IoT TCP 客户端
 * <p>
 * 负责与外部 TCP 服务器建立连接并发送设备消息
 * 支持 JSON 和 BINARY 两种数据格式，支持 SSL 加密连接
 *
 * @author HUIHUI
 */
@Slf4j
public class IotTcpClient {

    private final String host;
    private final Integer port;
    private final Integer connectTimeoutMs;
    private final Integer readTimeoutMs;
    private final Boolean ssl;
    private final String sslCertPath;
    private final String dataFormat;

    private Socket socket;
    private OutputStream outputStream;
    private BufferedReader reader;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    // TODO @puhui999：default 值，IotDataSinkTcpConfig.java 枚举起来哈；
    public IotTcpClient(String host, Integer port, Integer connectTimeoutMs, Integer readTimeoutMs,
                        Boolean ssl, String sslCertPath, String dataFormat) {
        this.host = host;
        this.port = port;
        this.connectTimeoutMs = connectTimeoutMs != null ? connectTimeoutMs : 5000;
        this.readTimeoutMs = readTimeoutMs != null ? readTimeoutMs : 10000;
        this.ssl = ssl != null ? ssl : false;
        this.sslCertPath = sslCertPath;
        this.dataFormat = dataFormat != null ? dataFormat : "JSON";
    }

    /**
     * 连接到 TCP 服务器
     */
    public void connect() throws Exception {
        if (connected.get()) {
            log.warn("[connect][TCP 客户端已经连接，无需重复连接]");
            return;
        }

        try {
            if (ssl) {
                // SSL 连接
                SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                socket = sslSocketFactory.createSocket();
            } else {
                // 普通连接
                socket = new Socket();
            }

            // 连接服务器
            socket.connect(new InetSocketAddress(host, port), connectTimeoutMs);
            socket.setSoTimeout(readTimeoutMs);

            // 获取输入输出流
            outputStream = socket.getOutputStream();
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            // 更新状态
            connected.set(true);
            log.info("[connect][TCP 客户端连接成功，服务器地址: {}:{}]", host, port);
        } catch (Exception e) {
            close();
            log.error("[connect][TCP 客户端连接失败，服务器地址: {}:{}]", host, port, e);
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
        if (!connected.get()) {
            throw new IllegalStateException("TCP 客户端未连接");
        }

        try {
            // TODO @puhui999：枚举值
            String messageData;
            if ("JSON".equalsIgnoreCase(dataFormat)) {
                // JSON 格式
                messageData = JsonUtils.toJsonString(message);
            } else {
                // BINARY 格式（这里简化为字符串，实际可能需要自定义二进制协议）
                messageData = message.toString();
            }

            // 发送消息
            outputStream.write(messageData.getBytes(StandardCharsets.UTF_8));
            outputStream.write('\n'); // 添加换行符作为消息分隔符
            outputStream.flush();
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
        if (!connected.get()) {
            return;
        }

        try {
            // 关闭资源
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("[close][关闭输入流失败]", e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.warn("[close][关闭输出流失败]", e);
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.warn("[close][关闭 Socket 失败]", e);
                }
            }

            // 更新状态
            connected.set(false);
            log.info("[close][TCP 客户端连接已关闭，服务器地址: {}:{}]", host, port);
        } catch (Exception e) {
            log.error("[close][关闭 TCP 客户端连接异常]", e);
        }
    }

    /**
     * 检查连接状态
     *
     * @return 是否已连接
     */
    public boolean isConnected() {
        return connected.get() && socket != null && !socket.isClosed();
    }

    @Override
    public String toString() {
        return "IotTcpClient{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", ssl=" + ssl +
                ", dataFormat='" + dataFormat + '\'' +
                ", connected=" + connected.get() +
                '}';
    }

}