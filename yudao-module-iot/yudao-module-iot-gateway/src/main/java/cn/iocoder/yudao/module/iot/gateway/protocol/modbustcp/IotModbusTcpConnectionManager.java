package cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT Modbus TCP 连接管理器，负责：
 * 1. 管理 TCP 连接（相同 ip:port 共用连接）
 * 2. 分布式锁管理（连接级别），避免多节点重复创建连接
 * 3. 连接重试和故障恢复
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotModbusTcpConnectionManager {

    // TODO @AI：iot:modbus-tcp:connection:
    private static final String LOCK_KEY_PREFIX = "iot:modbus:connection:";

    private final RedissonClient redissonClient;
    private final Vertx vertx;

    /**
     * 连接池：key = ip:port
     */
    private final Map<String, ModbusConnection> connectionPool = new ConcurrentHashMap<>();

    /**
     * 设备 ID 到连接 key 的映射
     */
    private final Map<Long, String> deviceConnectionMap = new ConcurrentHashMap<>();

    /**
     * 确保连接存在
     *
     * @param config 设备配置
     */
    public void ensureConnection(IotModbusDeviceConfigRespDTO config) {
        // 1. 记录设备与连接的关系
        String connectionKey = buildConnectionKey(config.getIp(), config.getPort());
        deviceConnectionMap.put(config.getDeviceId(), connectionKey);

        // 2. 情况一：连接已存在，添加设备引用
        ModbusConnection connection = connectionPool.get(connectionKey);
        if (connection != null) {
            // 添加设备引用
            connection.addDevice(config.getDeviceId(), config.getSlaveId());
            // 更新连接参数（取最小值）
            // TODO @AI：（不确定）如果后续最小值被移除后，是不是无法灰度到上一个最小值？
            connection.updateParams(config.getTimeout(), config.getRetryInterval());
            return;
        }

        // 3. 情况二：连接不存在，创建新连接
        // 3.1 尝试获取分布式锁
        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + connectionKey);
        if (!lock.tryLock()) {
            log.debug("[ensureConnection][获取锁失败, 由其他节点负责: {}]", connectionKey);
            return;
        }
        // 3.2 创建新连接
        try {
            connection = createConnection(config, lock);
            connectionPool.put(connectionKey, connection);
            log.info("[ensureConnection][创建 Modbus 连接成功: {}]", connectionKey);
        } catch (Exception e) {
            log.error("[ensureConnection][创建 Modbus 连接失败: {}]", connectionKey, e);
            lock.unlock();
        }
    }

    /**
     * 创建 Modbus TCP 连接
     */
    private ModbusConnection createConnection(IotModbusDeviceConfigRespDTO config, RLock lock) throws Exception {
        // 创建 TCP 连接
        // TODO @AI：需要重连么？
        TCPMasterConnection tcpConnection = new TCPMasterConnection(InetAddress.getByName(config.getIp()));
        tcpConnection.setPort(config.getPort());
        tcpConnection.setTimeout(config.getTimeout());
        tcpConnection.connect();

        // 创建 Modbus 连接对象
        ModbusConnection connection = new ModbusConnection();
        // TODO @AI：链式调用，简化下；
        connection.setConnectionKey(buildConnectionKey(config.getIp(), config.getPort()));
        connection.setTcpConnection(tcpConnection);
        connection.setLock(lock);
        connection.setTimeout(config.getTimeout());
        connection.setRetryInterval(config.getRetryInterval());
        connection.setContext(vertx.getOrCreateContext());
        connection.addDevice(config.getDeviceId(), config.getSlaveId());
        return connection;
    }

    /**
     * 获取连接
     */
    public ModbusConnection getConnection(Long deviceId) {
        String connectionKey = deviceConnectionMap.get(deviceId);
        if (connectionKey == null) {
            return null;
        }
        return connectionPool.get(connectionKey);
    }

    /**
     * 获取设备的 slave ID
     */
    public Integer getSlaveId(Long deviceId) {
        ModbusConnection connection = getConnection(deviceId);
        if (connection == null) {
            return null;
        }
        return connection.getSlaveId(deviceId);
    }

    /**
     * 移除设备
     */
    public void removeDevice(Long deviceId) {
        // 1. 移除设备引用
        String connectionKey = deviceConnectionMap.remove(deviceId);
        if (connectionKey == null) {
            return;
        }
        ModbusConnection connection = connectionPool.get(connectionKey);
        if (connection == null) {
            return;
        }
        connection.removeDevice(deviceId);

        // 2. 如果没有设备引用了，关闭连接
        if (connection.getDeviceCount() == 0) {
            closeConnection(connectionKey);
        }
    }

    /**
     * 关闭指定连接
     */
    private void closeConnection(String connectionKey) {
        ModbusConnection connection = connectionPool.remove(connectionKey);
        if (connection == null) {
            return;
        }

        try {
            if (connection.getTcpConnection() != null) {
                connection.getTcpConnection().close();
            }
            // TODO @AI：（不确定）是不是要当前线程？还是当前进程就 ok 了。
            if (connection.getLock() != null && connection.getLock().isHeldByCurrentThread()) {
                connection.getLock().unlock();
            }
            log.info("[closeConnection][关闭 Modbus 连接: {}]", connectionKey);
        } catch (Exception e) {
            log.error("[closeConnection][关闭连接失败: {}]", connectionKey, e);
        }
    }

    /**
     * 关闭所有连接
     */
    public void closeAll() {
        for (String connectionKey : connectionPool.keySet()) {
            closeConnection(connectionKey);
        }
        deviceConnectionMap.clear();
    }

    private String buildConnectionKey(String ip, Integer port) {
        return ip + ":" + port;
    }

    /**
     * Modbus 连接信息
     */
    @Data
    public static class ModbusConnection {

        private String connectionKey;
        private TCPMasterConnection tcpConnection;
        private RLock lock;
        private Integer timeout;
        private Integer retryInterval;
        private Context context;

        /**
         * 设备 ID 到 slave ID 的映射
         */
        private final Map<Long, Integer> deviceSlaveMap = new ConcurrentHashMap<>();

        public void addDevice(Long deviceId, Integer slaveId) {
            deviceSlaveMap.put(deviceId, slaveId);
        }

        public void removeDevice(Long deviceId) {
            deviceSlaveMap.remove(deviceId);
        }

        public int getDeviceCount() {
            return deviceSlaveMap.size();
        }

        public Integer getSlaveId(Long deviceId) {
            return deviceSlaveMap.get(deviceId);
        }

        public void updateParams(Integer timeout, Integer retryInterval) {
            // 取最小值
            if (timeout != null && (this.timeout == null || timeout < this.timeout)) {
                this.timeout = timeout;
            }
            if (retryInterval != null && (this.retryInterval == null || retryInterval < this.retryInterval)) {
                this.retryInterval = retryInterval;
            }
        }

        /**
         * 执行 Modbus 读取操作（阻塞方式，在 Vert.x worker 线程执行）
         */
        public <T> Future<T> executeBlocking(java.util.function.Function<TCPMasterConnection, T> operation) {
            // ordered=true 保证同一 Context 的操作串行执行，不同连接之间可并行
            return context.executeBlocking(() -> operation.apply(tcpConnection), true);
        }
    }

}
