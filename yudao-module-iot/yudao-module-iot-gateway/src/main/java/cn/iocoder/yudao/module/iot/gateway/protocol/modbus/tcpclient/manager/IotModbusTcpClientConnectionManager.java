package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusTcpClientUtils;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotWorkerTaskQueue;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.MODBUS_DISCONNECTED;

/**
 * IoT Modbus TCP 连接管理器
 * <p>
 * 统一管理 Modbus TCP 连接：
 * 1. 管理 TCP 连接（相同 ip:port 共用连接）
 * 2. 分布式锁管理（连接级别），避免多节点重复创建连接
 * 3. 连接重试和故障恢复
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpClientConnectionManager {

    private static final String LOCK_KEY_PREFIX = "iot:modbus-tcp:connection:";

    private final RedissonClient redissonClient;
    private final Vertx vertx;
    private final IotDeviceMessageService messageService;
    private final IotModbusTcpClientConfigCacheService configCacheService;
    private final String serverId;

    /**
     * 连接池：key = ip:port
     */
    private final Map<String, ModbusConnection> connectionPool = new ConcurrentHashMap<>();

    /**
     * 设备 ID 到连接 key 的映射
     */
    private final Map<Long, String> deviceConnectionMap = new ConcurrentHashMap<>();

    public IotModbusTcpClientConnectionManager(RedissonClient redissonClient, Vertx vertx,
                                                IotDeviceMessageService messageService,
                                                IotModbusTcpClientConfigCacheService configCacheService,
                                                String serverId) {
        this.redissonClient = redissonClient;
        this.vertx = vertx;
        this.messageService = messageService;
        this.configCacheService = configCacheService;
        this.serverId = serverId;
    }

    /**
     * 确保连接存在
     * <p>
     * 首次建连成功时，直接发送设备上线消息
     *
     * @param config 设备配置
     */
    public void ensureConnection(IotModbusDeviceConfigRespDTO config) {
        // 1.1 检查设备是否切换了 IP/端口，若是则先清理旧连接
        String connectionKey = buildConnectionKey(config.getIp(), config.getPort());
        String oldConnectionKey = deviceConnectionMap.get(config.getDeviceId());
        if (oldConnectionKey != null && ObjUtil.notEqual(oldConnectionKey, connectionKey)) {
            log.info("[ensureConnection][设备 {} IP/端口变更: {} -> {}, 清理旧连接]",
                    config.getDeviceId(), oldConnectionKey, connectionKey);
            removeDevice(config.getDeviceId());
        }
        // 1.2 记录设备与连接的映射
        deviceConnectionMap.put(config.getDeviceId(), connectionKey);

        // 2. 情况一：连接已存在，注册设备并发送上线消息
        ModbusConnection connection = connectionPool.get(connectionKey);
        if (connection != null) {
            addDeviceAndOnline(connection, config);
            return;
        }

        // 3. 情况二：连接不存在，加分布式锁创建新连接
        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + connectionKey);
        if (!lock.tryLock()) {
            log.debug("[ensureConnection][获取锁失败, 由其他节点负责: {}]", connectionKey);
            return;
        }
        try {
            // 3.1 double-check：拿到锁后再次检查，避免并发创建重复连接
            connection = connectionPool.get(connectionKey);
            if (connection != null) {
                addDeviceAndOnline(connection, config);
                lock.unlock();
                return;
            }
            // 3.2 创建新连接
            connection = createConnection(config);
            connection.setLock(lock);
            connectionPool.put(connectionKey, connection);
            log.info("[ensureConnection][创建 Modbus 连接成功: {}]", connectionKey);
            // 3.3 注册设备并发送上线消息
            addDeviceAndOnline(connection, config);
        } catch (Exception e) {
            log.error("[ensureConnection][创建 Modbus 连接失败: {}]", connectionKey, e);
            // 建连失败，释放锁让其他节点可重试
            lock.unlock();
        }
    }

    /**
     * 创建 Modbus TCP 连接
     */
    private ModbusConnection createConnection(IotModbusDeviceConfigRespDTO config) throws Exception {
        // 1. 创建 TCP 连接
        TCPMasterConnection tcpConnection = new TCPMasterConnection(InetAddress.getByName(config.getIp()));
        tcpConnection.setPort(config.getPort());
        tcpConnection.setTimeout(config.getTimeout());
        tcpConnection.connect();

        // 2. 创建 Modbus 连接对象
        return new ModbusConnection()
                .setConnectionKey(buildConnectionKey(config.getIp(), config.getPort()))
                .setTcpConnection(tcpConnection).setContext(vertx.getOrCreateContext())
                .setTimeout(config.getTimeout()).setRetryInterval(config.getRetryInterval());
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
     * <p>
     * 移除时直接发送设备下线消息
     */
    public void removeDevice(Long deviceId) {
        // 1.1 移除设备时，发送下线消息
        sendOfflineMessage(deviceId);
        // 1.2 移除设备引用
        String connectionKey = deviceConnectionMap.remove(deviceId);
        if (connectionKey == null) {
            return;
        }

        // 2.1 移除连接中的设备引用
        ModbusConnection connection = connectionPool.get(connectionKey);
        if (connection == null) {
            return;
        }
        connection.removeDevice(deviceId);
        // 2.2 如果没有设备引用了，关闭连接
        if (connection.getDeviceCount() == 0) {
            closeConnection(connectionKey);
        }
    }

    // ==================== 设备连接 & 上下线消息 ====================

    /**
     * 注册设备到连接，并发送上线消息
     */
    private void addDeviceAndOnline(ModbusConnection connection,
                                    IotModbusDeviceConfigRespDTO config) {
        Integer previous = connection.addDevice(config.getDeviceId(), config.getSlaveId());
        // 首次注册，发送上线消息
        if (previous == null) {
            sendOnlineMessage(config);
        }
    }

    /**
     * 发送设备上线消息
     */
    private void sendOnlineMessage(IotModbusDeviceConfigRespDTO config) {
        try {
            IotDeviceMessage onlineMessage = IotDeviceMessage.buildStateUpdateOnline();
            messageService.sendDeviceMessage(onlineMessage,
                    config.getProductKey(), config.getDeviceName(), serverId);
        } catch (Exception ex) {
            log.error("[sendOnlineMessage][发送设备上线消息失败, deviceId={}]", config.getDeviceId(), ex);
        }
    }

    /**
     * 发送设备下线消息
     */
    private void sendOfflineMessage(Long deviceId) {
        IotModbusDeviceConfigRespDTO config = configCacheService.getConfig(deviceId);
        if (config == null) {
            return;
        }
        try {
            IotDeviceMessage offlineMessage = IotDeviceMessage.buildStateOffline();
            messageService.sendDeviceMessage(offlineMessage,
                    config.getProductKey(), config.getDeviceName(), serverId);
        } catch (Exception ex) {
            log.error("[sendOfflineMessage][发送设备下线消息失败, deviceId={}]", deviceId, ex);
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
        // 先标记失效：已经排进有序队列、尚未执行的任务直接 115，不会在被关闭的连接上重新 connect()
        connection.setRetired(true);

        try {
            if (connection.getTcpConnection() != null) {
                connection.getTcpConnection().close();
            }
            // 释放分布式锁，让其他节点可接管
            RLock lock = connection.getLock();
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
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
        // 先复制再遍历，避免 closeConnection 中 remove 导致并发修改
        List<String> connectionKeys = new ArrayList<>(connectionPool.keySet());
        for (String connectionKey : connectionKeys) {
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
        private Integer timeout;
        private Integer retryInterval;
        /**
         * 设备 ID 到 slave ID 的映射
         */
        private final Map<Long, Integer> deviceSlaveMap = new ConcurrentHashMap<>();

        /**
         * 分布式锁，锁住连接的创建和销毁，避免多节点重复连接同一从站
         */
        private RLock lock;

        /**
         * Vert.x Context，用于 executeBlocking 执行 Modbus 操作，保证同一连接的操作串行执行
         */
        private Context context;
        /**
         * 本连接读取结果的上报队列（{@link #context} 上的有序有界 worker 队列，随 {@link #setContext} 创建）：
         * 点位翻译、发送到消息总线会阻塞，交给 worker 按读取完成的顺序执行，不占用 context 的 event loop；
         * 只在 context 线程上提交（executeBlocking 的结果回调就在它上面）
         */
        @Setter(AccessLevel.NONE)
        private IotWorkerTaskQueue upstreamQueue;

        /**
         * 是否已失效：closeConnection 移出连接池时置为 true；之后开始执行的任务直接 115
         */
        private volatile boolean retired;

        /**
         * 透传请求的在途 + 排队计数（只计 raw；轮询和标准读写不计入）
         */
        private final AtomicInteger rawOutstanding = new AtomicInteger();

        /**
         * 设置执行 Modbus 操作的 Context，同时创建它上面的上报队列
         *
         * @param context Vert.x Context
         * @return this
         */
        public ModbusConnection setContext(Context context) {
            this.context = context;
            this.upstreamQueue = new IotWorkerTaskQueue(context);
            return this;
        }

        public Integer addDevice(Long deviceId, Integer slaveId) {
            return deviceSlaveMap.putIfAbsent(deviceId, slaveId);
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

        /**
         * 执行标准 Modbus 操作（轮询读取、标准写入；阻塞方式，在 Vert.x worker 线程执行）
         * <p>
         * 与原逻辑一致：操作失败（包括读超时）只抛出，不关闭这个可能被多个从站共用的物理连接，
         * 未连接时由 j2mod 的 ModbusTCPTransaction.execute 自己 connect。
         * 只多一个检查：已被 closeConnection 移出连接池（retired）时，排队中的任务不再执行，直接 115，不会重新连上被关闭的连接
         */
        public <T> Future<T> executeBlocking(Function<TCPMasterConnection, T> operation) {
            // ordered=true 保证同一 Context 的操作串行执行，不同连接之间可并行
            return context.executeBlocking(() -> {
                if (retired) {
                    throw exception(MODBUS_DISCONNECTED);
                }
                return operation.apply(tcpConnection);
            }, true);
        }

        /**
         * 执行透传操作（阻塞方式，在 Vert.x worker 线程执行；与标准操作排在同一个有序队列里）
         * <p>
         * 只有透传使用的传输失效隔离，在有序边界内完成（不在结果投递或 onComplete 之后重连）：
         * 1. 已被 closeConnection 移出连接池（retired）：排队中的任务不再执行，直接 115；
         * 2. 之前的透传因传输失败关闭了连接：本任务开始时用现有的 connect() 恢复；
         * 3. 传输失败（读超时、IO 异常）：在同一个有序任务里先关闭连接，再抛出异常，迟到的响应不会被下一个事务读到；
         *    设备异常响应（ModbusSlaveException）说明链路正常，不关闭、不重连
         */
        public <T> Future<T> executeRawBlocking(Function<TCPMasterConnection, T> operation) {
            return context.executeBlocking(() -> {
                // 1. 连接已被 closeConnection 移出连接池：排队中的任务不再执行
                if (retired) {
                    throw exception(MODBUS_DISCONNECTED);
                }
                // 2. 之前的透传因传输失败关闭了连接：在本任务里用现有的 connect() 恢复
                //    （j2mod 的 ModbusTCPTransaction.execute 也会在未连接时 connect；这里先连上，它看到已连接就不会再连一次）
                if (!tcpConnection.isConnected()) {
                    tcpConnection.connect();
                }
                // 3. 执行操作：传输失败时先让连接失效，之后的任务开始时才会重连
                try {
                    return operation.apply(tcpConnection);
                } catch (RuntimeException e) {
                    if (IotModbusTcpClientUtils.isTransportFailure(e)) {
                        tcpConnection.close();
                    }
                    throw e;
                }
            }, true);
        }

        /**
         * 占用一个透传名额
         *
         * @param max 每个连接的上限
         * @return 是否成功；false 时调用方直接返回 113 失败，不进入 executeBlocking
         */
        public boolean tryAcquireRawSlot(int max) {
            if (rawOutstanding.incrementAndGet() <= max) {
                return true;
            }
            rawOutstanding.decrementAndGet();
            return false;
        }

        /**
         * 释放一个透传名额（透传任务到达终态时调用）
         */
        public void releaseRawSlot() {
            rawOutstanding.decrementAndGet();
        }

    }

}
