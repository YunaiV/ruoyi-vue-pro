package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.client;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP 设备客户端：封装设备连接的基本信息和操作
 * <p>
 * 该类中的状态变更（如 authenticated, closed）使用 AtomicBoolean 保证原子性。
 * 对 socket 的操作应在 Vert.x Event Loop 线程中执行，以避免并发问题。
 *
 * @author 芋道源码
 */
@Slf4j
public class TcpDeviceClient {

    @Getter
    private final String clientId;

    @Getter
    @Setter
    private String deviceAddr; // 从 final 移除，因为在注册后才设置

    @Getter
    @Setter
    private String productKey;

    @Getter
    @Setter
    private String deviceName;

    @Getter
    @Setter
    private Long deviceId;

    @Getter
    private NetSocket socket;

    @Getter
    @Setter
    private RecordParser parser;

    @Getter
    private final long keepAliveTimeoutMs;

    private volatile long lastKeepAliveTime;

    private final AtomicBoolean authenticated = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * 构造函数
     *
     * @param clientId           客户端 ID，全局唯一
     * @param keepAliveTimeoutMs 心跳超时时间（毫秒），从配置中读取
     */
    public TcpDeviceClient(String clientId, long keepAliveTimeoutMs) {
        this.clientId = clientId;
        this.keepAliveTimeoutMs = keepAliveTimeoutMs;
        this.lastKeepAliveTime = System.currentTimeMillis();
    }

    /**
     * 绑定网络套接字，并设置相关处理器。
     * 此方法应在 Vert.x Event Loop 线程中调用
     *
     * @param socket 网络套接字
     */
    public void setSocket(NetSocket socket) {
        // 无需 synchronized，Vert.x 保证了同一个 socket 的事件在同一个 Event Loop 中处理
        if (this.socket != null && this.socket != socket) {
            log.warn("[setSocket][客户端({}) 正在用新的 socket 替换旧的，旧 socket 将被关闭]", clientId);
            this.socket.close();
        }
        this.socket = socket;

        // 注册处理器
        if (socket != null) {
            // 1. 设置关闭处理器
            socket.closeHandler(v -> {
                log.info("[setSocket][设备客户端({})的连接已由远端关闭]", clientId);
                shutdown(); // 统一调用 shutdown 进行资源清理
            });

            // 2. 设置异常处理器
            socket.exceptionHandler(e -> {
                log.error("[setSocket][设备客户端({})连接出现异常]", clientId, e);
                shutdown(); // 出现异常时也关闭连接
            });

            // 3. 设置数据处理器
            socket.handler(buffer -> {
                // 任何数据往来都表示连接是活跃的
                keepAlive();

                if (parser != null) {
                    parser.handle(buffer);
                } else {
                    log.warn("[setSocket][设备客户端({}) 未设置解析器(parser)，原始数据被忽略: {}]", clientId, buffer.toString());
                }
            });
        }
    }

    /**
     * 更新心跳时间，表示设备仍然活跃
     */
    public void keepAlive() {
        this.lastKeepAliveTime = System.currentTimeMillis();
    }

    /**
     * 检查连接是否在线
     * 判断标准：未被主动关闭、socket 存在、且在心跳超时时间内
     *
     * @return 是否在线
     */
    public boolean isOnline() {
        if (closed.get() || socket == null) {
            return false;
        }
        long idleTime = System.currentTimeMillis() - lastKeepAliveTime;
        return idleTime < keepAliveTimeoutMs;
    }

    // TODO @haohao：1）是不是简化下：productKey 和 deviceName 非空，就认为是已认证；2）如果是的话，productKey 和 deviceName 搞成一个设置方法？setAuthenticated（productKey、deviceName）

    public boolean isAuthenticated() {
        return authenticated.get();
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated.set(authenticated);
    }

    /**
     * 向设备发送消息
     *
     * @param buffer 消息内容
     */
    public void sendMessage(Buffer buffer) {
        if (closed.get() || socket == null) {
            log.warn("[sendMessage][设备客户端({})连接已关闭，无法发送消息]", clientId);
            return;
        }

        // Vert.x 的 write 是异步的，不会阻塞
        socket.write(buffer, result -> {
            // 发送失败可能意味着连接已断开，主动关闭
            if (!result.succeeded()) {
                log.error("[sendMessage][设备客户端({})发送消息失败]", clientId, result.cause());
                shutdown();
                return;
            }

            // 发送成功也更新心跳，表示连接活跃
            if (log.isDebugEnabled()) {
                log.debug("[sendMessage][设备客户端({})发送消息成功]", clientId);
            }
            keepAlive();
        });
    }

    // TODO @haohao：是不是叫 close 好点？或者问问大模型
    /**
     * 关闭客户端连接并清理资源。
     * 这是一个幂等操作，可以被多次安全调用。
     */
    public void shutdown() {
        // 使用原子操作保证只执行一次关闭逻辑
        if (closed.getAndSet(true)) {
            return;
        }

        log.info("[shutdown][正在关闭设备客户端连接: {}]", clientId);

        // 先将 socket 引用置空，再关闭，避免并发问题
        NetSocket socketToClose = this.socket;
        this.socket = null;

        if (socketToClose != null) {
            try {
                // close 是异步的，但我们在这里不关心其结果，因为我们已经将客户端标记为关闭
                socketToClose.close();
            } catch (Exception e) {
                log.warn("[shutdown][关闭TCP连接时出现异常，可能已被关闭]", e);
            }
        }

        // 重置认证状态
        authenticated.set(false);
    }

    public String getConnectionInfo() {
        NetSocket currentSocket = this.socket;
        if (currentSocket != null && currentSocket.remoteAddress() != null) {
            return currentSocket.remoteAddress().toString();
        }
        return "disconnected";
    }

    @Override
    public String toString() {
        return "TcpDeviceClient{" +
                "clientId='" + clientId + '\'' +
                ", deviceAddr='" + deviceAddr + '\'' +
                ", deviceId=" + deviceId +
                ", authenticated=" + authenticated.get() +
                ", online=" + isOnline() +
                ", connection=" + getConnectionInfo() +
                '}';
    }

}