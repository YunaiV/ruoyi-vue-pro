package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConnectionManager.ModbusConnection;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link IotModbusTcpClientConnectionManager} 的单元测试：属性设置写入前核对的连接目标（IP、端口、从站号）
 * <p>
 * 用本机的 {@link ServerSocket} 充当从站端口：j2mod 建立 TCP 连接即可，不收发 Modbus 报文
 *
 * @author 芋道源码
 */
public class IotModbusTcpClientConnectionManagerTest {

    private static final Long DEVICE_ID = 1L;

    private Vertx vertx;
    private ServerSocket server1;
    private ServerSocket server2;
    private IotModbusTcpClientConnectionManager connectionManager;

    @BeforeEach
    public void setUp() throws Exception {
        vertx = Vertx.vertx();
        InetAddress loopback = InetAddress.getLoopbackAddress();
        server1 = new ServerSocket(0, 10, loopback);
        server2 = new ServerSocket(0, 10, loopback);
        RedissonClient redissonClient = mock(RedissonClient.class);
        RLock lock = mock(RLock.class);
        when(lock.tryLock()).thenReturn(true);
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        connectionManager = new IotModbusTcpClientConnectionManager(redissonClient, vertx,
                mock(IotDeviceMessageService.class), mock(IotModbusTcpClientConfigCacheService.class), "server-1");
    }

    @AfterEach
    public void tearDown() throws Exception {
        connectionManager.closeAll();
        server1.close();
        server2.close();
        vertx.close().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
    }

    @Test
    public void testIsCurrentTarget_slaveIdChanged() {
        // 准备参数：设备 1 连接到 server1，从站号 1
        IotModbusDeviceConfigRespDTO config = newConfig(server1.getLocalPort(), 1);
        connectionManager.ensureConnection(config);
        ModbusConnection connection = connectionManager.getConnection(DEVICE_ID);
        assertNotNull(connection);
        assertTrue(connectionManager.isCurrentTarget(config, connection, 1));

        // 调用：配置缓存已更新为从站号 2，连接还没刷新
        IotModbusDeviceConfigRespDTO newConfig = newConfig(server1.getLocalPort(), 2);
        // 断言：旧从站号的写入不再执行
        assertFalse(connectionManager.isCurrentTarget(newConfig, connection, 1));

        // 调用：连接刷新
        connectionManager.ensureConnection(newConfig);
        // 断言：同一连接，登记的从站号已更新；只有新从站号的写入可以执行
        assertSame(connection, connectionManager.getConnection(DEVICE_ID));
        assertEquals(2, connectionManager.getSlaveId(DEVICE_ID));
        assertFalse(connectionManager.isCurrentTarget(newConfig, connection, 1));
        assertTrue(connectionManager.isCurrentTarget(newConfig, connection, 2));
        // 断言：设备已删除（配置为空）
        assertFalse(connectionManager.isCurrentTarget(null, connection, 2));
    }

    @Test
    public void testIsCurrentTarget_addressChanged() {
        // 准备参数：设备 1 连接到 server1
        IotModbusDeviceConfigRespDTO config = newConfig(server1.getLocalPort(), 1);
        connectionManager.ensureConnection(config);
        ModbusConnection oldConnection = connectionManager.getConnection(DEVICE_ID);

        // 调用：配置缓存已更新为 server2 的端口，连接还没刷新
        IotModbusDeviceConfigRespDTO newConfig = newConfig(server2.getLocalPort(), 1);
        // 断言：旧连接上的写入不再执行
        assertFalse(connectionManager.isCurrentTarget(newConfig, oldConnection, 1));

        // 调用：连接刷新，迁移到新连接
        connectionManager.ensureConnection(newConfig);
        ModbusConnection newConnection = connectionManager.getConnection(DEVICE_ID);
        // 断言：旧连接已失效；只有新连接上的写入可以执行
        assertNotSame(oldConnection, newConnection);
        assertTrue(oldConnection.isRetired());
        assertFalse(connectionManager.isCurrentTarget(newConfig, oldConnection, 1));
        assertTrue(connectionManager.isCurrentTarget(newConfig, newConnection, 1));
    }

    private static IotModbusDeviceConfigRespDTO newConfig(int port, int slaveId) {
        IotModbusDeviceConfigRespDTO config = new IotModbusDeviceConfigRespDTO();
        config.setDeviceId(DEVICE_ID);
        config.setProductKey("pk");
        config.setDeviceName("dn");
        config.setIp(InetAddress.getLoopbackAddress().getHostAddress());
        config.setPort(port);
        config.setSlaveId(slaveId);
        config.setTimeout(1000);
        config.setRetryInterval(1000);
        return config;
    }

}
