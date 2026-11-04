package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils;

import cn.hutool.core.util.HexUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusRawDataTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager.IotModbusTx;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConnectionManager.ModbusConnection;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.ModbusSlaveException;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterRequest;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils.FC_READ_HOLDING_REGISTERS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link IotModbusTcpClientUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class IotModbusTcpClientUtilsTest {

    private static final int SLAVE_ID = 1;
    /**
     * 03 00 10 00 02：读 2 个保持寄存器
     */
    private static final String READ_PDU = "0300100002";

    private Vertx vertx;
    private TCPMasterConnection tcpConnection;
    private AbstractModbusTransport transport;
    private ModbusConnection connection;

    /**
     * 写出时请求上的事务号（setRequest 分配）
     */
    private final AtomicReference<Integer> sentTransactionId = new AtomicReference<>();
    /**
     * 写出的请求：execute 结束后它的事务号会被 j2mod 自增
     */
    private final AtomicReference<ModbusRequest> sentRequest = new AtomicReference<>();

    @BeforeEach
    public void setUp() throws Exception {
        vertx = Vertx.vertx();
        tcpConnection = mock(TCPMasterConnection.class);
        transport = mock(AbstractModbusTransport.class);
        when(tcpConnection.isConnected()).thenReturn(true);
        when(tcpConnection.getModbusTransport()).thenReturn(transport);
        when(tcpConnection.getTimeout()).thenReturn(1000);
        doAnswer(invocation -> {
            ModbusRequest request = invocation.getArgument(0);
            sentRequest.set(request);
            sentTransactionId.set(request.getTransactionID());
            return null;
        }).when(transport).writeRequest(any(ModbusRequest.class));
        connection = new ModbusConnection();
        connection.setTcpConnection(tcpConnection);
        connection.setContext(vertx.getOrCreateContext());
    }

    @AfterEach
    public void tearDown() throws Exception {
        vertx.close().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
    }

    @Test
    public void testIsTransportFailure() {
        // 传输失败：沿 cause 链找到 ModbusIOException / IOException（read / write 会包一层 RuntimeException）
        assertTrue(IotModbusTcpClientUtils.isTransportFailure(
                new RuntimeException("Modbus 读取失败", new ModbusIOException("Socket timeout reading response - Read timed out"))));
        assertTrue(IotModbusTcpClientUtils.isTransportFailure(new RuntimeException(new SocketTimeoutException())));
        assertTrue(IotModbusTcpClientUtils.isTransportFailure(new ConnectException("Connection refused")));
        // 设备异常响应：链路正常
        assertFalse(IotModbusTcpClientUtils.isTransportFailure(new RuntimeException(new ModbusSlaveException(2))));
        // 网关自己判定的失败（排队过期、响应不合法、连接已失效）
        assertFalse(IotModbusTcpClientUtils.isTransportFailure(exception(MODBUS_RESPONSE_INVALID)));
        assertFalse(IotModbusTcpClientUtils.isTransportFailure(null));
    }

    @Test
    public void testToRawFailure() {
        // ServiceException：保留错误码
        assertEquals(MODBUS_DISCONNECTED.getCode(),
                IotModbusTcpClientUtils.toRawFailure(exception(MODBUS_DISCONNECTED)).getCode());
        // j2mod 读超时（cause 已丢失，只能按消息判断）→ 110
        assertEquals(MODBUS_TIMEOUT.getCode(), IotModbusTcpClientUtils.toRawFailure(new RuntimeException(
                new ModbusIOException("Executing transaction 00 failed (tried 1 times) Socket timeout reading response - Read timed out")))
                .getCode());
        // 其它 IO 失败 → 106
        assertEquals(DEVICE_OFFLINE_OR_WRITE_FAILED.getCode(),
                IotModbusTcpClientUtils.toRawFailure(new ConnectException("Connection refused")).getCode());
    }

    // ========== 标准读写：保持原逻辑，超时不关闭共享连接 ==========

    @Test
    public void testExecuteBlocking_standardTimeoutKeepSharedConnection() throws Exception {
        // mock 方法：同一个物理连接上的从站 2 照常回正常响应
        mockResponse(0, SLAVE_ID + 1, "0304" + "00FA0258");

        // 调用：从站 1 的标准读取超时（与 read 一样，把 j2mod 的 ModbusIOException 包一层 RuntimeException 抛出）
        Future<int[]> timeout = await(connection.<int[]>executeBlocking(wire -> {
            throw new RuntimeException("Modbus 读取失败 [slaveId=1]",
                    new ModbusIOException("Socket timeout reading response - Read timed out"));
        }));
        // 调用：之后从站 2 的标准读取
        Future<int[]> next = await(IotModbusTcpClientUtils.read(connection, SLAVE_ID + 1, 3, 0x10, 2));

        // 断言：超时照常失败，但不关闭、不重连共享的物理连接；从站 2 的读取照常完成
        assertTrue(timeout.failed());
        assertTrue(IotModbusTcpClientUtils.isTransportFailure(timeout.cause()));
        assertTrue(next.succeeded());
        assertArrayEquals(new int[]{0x00FA, 0x0258}, next.result());
        verify(tcpConnection, never()).close();
        verify(tcpConnection, never()).connect();
    }

    @Test
    public void testExecuteBlocking_retired() throws Exception {
        // 准备参数：连接已被 closeConnection 移出连接池
        connection.setRetired(true);

        // 调用
        Future<int[]> result = await(IotModbusTcpClientUtils.read(connection, SLAVE_ID, 3, 0x10, 2));

        // 断言：排队中的标准任务不再执行，直接 115，不会重新连上被关闭的连接
        assertTrue(result.failed());
        assertInstanceOf(ServiceException.class, result.cause());
        assertEquals(MODBUS_DISCONNECTED.getCode(), ((ServiceException) result.cause()).getCode());
        verify(transport, never()).writeRequest(any(ModbusRequest.class));
        verify(tcpConnection, never()).connect();
    }

    // ========== executeRaw：传输失败 ==========

    @Test
    public void testExecuteRaw_timeoutCloseInOrderedTask() throws Exception {
        // mock 方法：读响应超时（j2mod 的 ModbusTCPTransport 把 SocketTimeoutException 转为 ModbusIOException）
        when(transport.readResponse()).thenThrow(new ModbusIOException("Socket timeout reading response - Read timed out"));

        // 调用
        Future<byte[]> result = executeRaw();

        // 断言：只尝试 1 次；透传的守卫在本有序任务内关闭连接（迟到的响应不会被下一个事务读到），转换为 110
        assertTrue(result.failed());
        assertEquals(MODBUS_TIMEOUT.getCode(), IotModbusTcpClientUtils.toRawFailure(result.cause()).getCode());
        verify(transport, times(1)).writeRequest(any(ModbusRequest.class));
        verify(tcpConnection, times(1)).close();
    }

    // ========== executeRaw：正常响应 ==========

    @Test
    public void testExecuteRaw_normalUseSavedIdentity() throws Exception {
        // mock 方法：设备按发出的事务号、unit 回正常响应
        mockResponse(0, SLAVE_ID, "0304" + "00FA0258");

        // 调用
        Future<byte[]> result = executeRaw();

        // 断言：用 execute 之前保存的身份核对通过；j2mod 已在 execute 之后把请求的事务号自增
        assertTrue(result.succeeded());
        assertArrayEquals(HexUtil.decodeHex("030400FA0258"), result.result());
        assertNotEquals(sentTransactionId.get().intValue(), sentRequest.get().getTransactionID());
        verify(tcpConnection, never()).close();
        // 断言：只尝试 1 次
        verify(transport, times(1)).writeRequest(any(ModbusRequest.class));
    }

    @Test
    public void testExecuteRaw_normalUnitMismatch() throws Exception {
        // mock 方法：事务号正确，unit 不对（j2mod 不核对 unit）
        mockResponse(0, SLAVE_ID + 1, "0304" + "00FA0258");

        // 调用
        Future<byte[]> result = executeRaw();

        // 断言：114，并在本有序任务内关闭连接
        assertFailedWith(result, MODBUS_RESPONSE_INVALID.getCode());
        verify(tcpConnection, times(1)).close();
    }

    @Test
    public void testExecuteRaw_normalTransactionIdMismatch() throws Exception {
        // mock 方法：事务号不对（j2mod 核对正常响应的事务号，只尝试 1 次就失败）
        mockResponse(1, SLAVE_ID, "0304" + "00FA0258");

        // 调用
        Future<byte[]> result = executeRaw();

        // 断言：传输失败，守卫在本有序任务内关闭连接；不重发
        assertTrue(result.failed());
        assertFalse(result.cause() instanceof ServiceException);
        verify(tcpConnection, times(1)).close();
        verify(transport, times(1)).writeRequest(any(ModbusRequest.class));
    }

    @Test
    public void testExecuteRaw_normalFunctionCodeMismatch() throws Exception {
        // mock 方法：事务号、unit 正确，功能码不对
        mockResponse(0, SLAVE_ID, "0404" + "00FA0258");

        // 调用
        Future<byte[]> result = executeRaw();

        // 断言
        assertFailedWith(result, MODBUS_RESPONSE_INVALID.getCode());
        verify(tcpConnection, times(1)).close();
    }

    // ========== executeRaw：异常响应 ==========

    @Test
    public void testExecuteRaw_exceptionCorrelated() throws Exception {
        // mock 方法：设备按发出的事务号、unit 回 83 02
        mockResponse(0, SLAVE_ID, "8302");

        // 调用
        Future<byte[]> result = executeRaw();

        // 断言：取 transaction.getResponse() 的真实异常响应，还原为 83 02，作为成功结果；链路正常不关闭
        assertTrue(result.succeeded());
        assertArrayEquals(HexUtil.decodeHex("8302"), result.result());
        verify(tcpConnection, never()).close();
    }

    @Test
    public void testExecuteRaw_exceptionTransactionIdMismatch() throws Exception {
        // mock 方法：异常响应的事务号不对（j2mod 在核对事务号之前就抛出 ModbusSlaveException）
        mockResponse(1, SLAVE_ID, "8302");

        // 调用
        Future<byte[]> result = executeRaw();

        // 断言：不按当前功能码 + 异常码拼出归属：114，并关闭连接
        assertFailedWith(result, MODBUS_RESPONSE_INVALID.getCode());
        verify(tcpConnection, times(1)).close();
    }

    @Test
    public void testExecuteRaw_exceptionUnitOrFunctionCodeMismatch() throws Exception {
        // mock 方法：unit 不对
        mockResponse(0, SLAVE_ID + 1, "8302");

        // 调用 + 断言
        assertFailedWith(executeRaw(), MODBUS_RESPONSE_INVALID.getCode());
        verify(tcpConnection, times(1)).close();

        // mock 方法：功能码不对（FC03 的请求收到 84 02）
        mockResponse(0, SLAVE_ID, "8402");

        // 调用 + 断言
        assertFailedWith(executeRaw(), MODBUS_RESPONSE_INVALID.getCode());
        verify(tcpConnection, times(2)).close();
    }

    // ========== writeBit：寄存器位写入（读-改-写） ==========

    @Test
    public void testWriteBit_readModifyWrite() throws Exception {
        // mock 方法：FC03 读到 0x5A00，FC06 原样回显
        mockBitWriteResponses("03025A00", false);

        // 调用：置第 10 位
        Future<Boolean> result = await(IotModbusTcpClientUtils.writeBit(connection, SLAVE_ID, randomBitPoint(10), true));

        // 断言：先 FC03 读 1 个寄存器，再 FC06 写回只改了第 10 位的值
        assertTrue(result.succeeded());
        ArgumentCaptor<ModbusRequest> captor = ArgumentCaptor.forClass(ModbusRequest.class);
        verify(transport, times(2)).writeRequest(captor.capture());
        ReadMultipleRegistersRequest readRequest = assertInstanceOf(ReadMultipleRegistersRequest.class, captor.getAllValues().get(0));
        assertEquals(0x10, readRequest.getReference());
        assertEquals(1, readRequest.getWordCount());
        assertEquals(SLAVE_ID, readRequest.getUnitID());
        WriteSingleRegisterRequest writeRequest = assertInstanceOf(WriteSingleRegisterRequest.class, captor.getAllValues().get(1));
        assertEquals(0x10, writeRequest.getReference());
        assertEquals(0x5E00, writeRequest.getRegister().getValue());
        assertEquals(SLAVE_ID, writeRequest.getUnitID());
    }

    @Test
    public void testWriteBit_writeTimeoutTriedOnce() throws Exception {
        // mock 方法：FC03 读到 0x5A00，FC06 读响应超时
        mockBitWriteResponses("03025A00", true);

        // 调用
        Future<Boolean> result = await(IotModbusTcpClientUtils.writeBit(connection, SLAVE_ID, randomBitPoint(10), true));

        // 断言：失败直接返回调用方；FC06 只写出 1 次，不用旧的读取结果重发
        assertTrue(result.failed());
        assertTrue(IotModbusTcpClientUtils.isTimeoutFailure(result.cause()));
        ArgumentCaptor<ModbusRequest> captor = ArgumentCaptor.forClass(ModbusRequest.class);
        verify(transport, times(2)).writeRequest(captor.capture());
        assertInstanceOf(ReadMultipleRegistersRequest.class, captor.getAllValues().get(0));
        assertInstanceOf(WriteSingleRegisterRequest.class, captor.getAllValues().get(1));
        // 断言：在本有序任务内关闭连接，丢弃可能晚到的回显
        verify(tcpConnection, times(1)).close();
    }

    @Test
    public void testWriteBit_readFailNotWrite() throws Exception {
        // mock 方法：FC03 收到异常响应 83 02
        mockResponse(0, SLAVE_ID, "8302");

        // 调用
        Future<Boolean> result = await(IotModbusTcpClientUtils.writeBit(connection, SLAVE_ID, randomBitPoint(10), true));

        // 断言：读取失败不写回；设备异常响应说明链路正常，不关闭连接
        assertTrue(result.failed());
        verify(transport, times(1)).writeRequest(any(ReadMultipleRegistersRequest.class));
        verify(transport, never()).writeRequest(any(WriteSingleRegisterRequest.class));
        verify(tcpConnection, never()).close();
    }

    @Test
    public void testWriteBit_networkSequentialAndConcurrent() throws Exception {
        // 准备参数：真实网络上的从站，保持寄存器 0x0010 = 0x5A00
        try (FakeRegisterSlave slave = new FakeRegisterSlave(0x5A00, 0)) {
            ModbusConnection networkConnection = connect(slave.getPort());
            try {
                // 调用：依次置第 10 位、清第 9 位
                assertTrue(await(IotModbusTcpClientUtils.writeBit(networkConnection, SLAVE_ID, randomBitPoint(10), true)).succeeded());
                assertEquals(0x5E00, slave.getRegister());
                assertTrue(await(IotModbusTcpClientUtils.writeBit(networkConnection, SLAVE_ID, randomBitPoint(9), false)).succeeded());
                // 断言：每次只改目标位
                assertEquals(0x5C00, slave.getRegister());

                // 调用：恢复为 0x5A00 后，同时提交置第 10 位、清第 9 位
                slave.setRegister(0x5A00);
                Future<Boolean> setBit10 = IotModbusTcpClientUtils.writeBit(networkConnection, SLAVE_ID, randomBitPoint(10), true);
                Future<Boolean> clearBit9 = IotModbusTcpClientUtils.writeBit(networkConnection, SLAVE_ID, randomBitPoint(9), false);
                // 断言：同一连接的有序队列里先后执行，两次修改都保留
                assertTrue(await(setBit10).succeeded());
                assertTrue(await(clearBit9).succeeded());
                assertEquals(0x5C00, slave.getRegister());
                assertEquals(4, slave.getWriteCount());
            } finally {
                networkConnection.getTcpConnection().close();
            }
        }
    }

    @Test
    public void testWriteBit_networkWriteAckLateThenRecover() throws Exception {
        // 准备参数：真实网络上的从站，FC06 的回显比网关的读超时（500ms）晚到
        try (FakeRegisterSlave slave = new FakeRegisterSlave(0x5A00, 800)) {
            ModbusConnection networkConnection = connect(slave.getPort());
            try {
                // 调用
                Future<Boolean> result = await(IotModbusTcpClientUtils.writeBit(networkConnection, SLAVE_ID, randomBitPoint(10), true));
                // 断言：读超时失败；从站只收到 1 次 FC06，不自动重发
                assertTrue(result.failed());
                assertTrue(IotModbusTcpClientUtils.isTimeoutFailure(result.cause()));
                assertEquals(1, slave.getWriteCount());

                // 调用：等晚到的回显发出后，在同一个连接上连续读取 2 次
                assertTrue(slave.awaitWriteAck());
                Future<int[]> read01 = await(IotModbusTcpClientUtils.read(networkConnection, SLAVE_ID, FC_READ_HOLDING_REGISTERS, 0x10, 1));
                Future<int[]> read02 = await(IotModbusTcpClientUtils.read(networkConnection, SLAVE_ID, FC_READ_HOLDING_REGISTERS, 0x10, 1));
                // 断言：超时时已关闭旧连接，晚到的回显不会被读到；重连后读到从站的实际值（FC06 已在从站生效）
                assertTrue(read01.succeeded());
                assertArrayEquals(new int[]{0x5E00}, read01.result());
                assertTrue(read02.succeeded());
                assertArrayEquals(new int[]{0x5E00}, read02.result());

                // 调用：同一个连接上再次位写入
                slave.setWriteAckDelayMillis(0);
                assertTrue(await(IotModbusTcpClientUtils.writeBit(networkConnection, SLAVE_ID, randomBitPoint(9), false)).succeeded());
                // 断言
                assertEquals(0x5C00, slave.getRegister());
                assertEquals(2, slave.getWriteCount());
            } finally {
                networkConnection.getTcpConnection().close();
            }
        }
    }

    // ========== 辅助方法 ==========

    /**
     * mock 传输层的读响应：事务号 = 写出的事务号 + transactionIdOffset
     *
     * @param transactionIdOffset 事务号偏移（0 表示正确）
     * @param unitId              响应的 unit
     * @param pduHex              响应 PDU
     */
    private void mockResponse(int transactionIdOffset, int unitId, String pduHex) throws Exception {
        doAnswer(invocation -> {
            byte[] pdu = HexUtil.decodeHex(pduHex);
            int transactionId = (sentTransactionId.get() + transactionIdOffset) & 0xFFFF;
            return readResponse(String.format("%04X0000%04X%02X", transactionId, pdu.length + 1, unitId) + pduHex);
        }).when(transport).readResponse();
    }

    /**
     * mock 位写入的读响应：FC03 回 readPduHex；FC06 按写出的请求原样回显，writeTimeout 为 true 时读响应超时
     */
    private void mockBitWriteResponses(String readPduHex, boolean writeTimeout) throws Exception {
        doAnswer(invocation -> {
            String pduHex = readPduHex;
            if (sentRequest.get() instanceof WriteSingleRegisterRequest) {
                if (writeTimeout) {
                    throw new ModbusIOException("Socket timeout reading response - Read timed out");
                }
                WriteSingleRegisterRequest writeRequest = (WriteSingleRegisterRequest) sentRequest.get();
                pduHex = String.format("06%04X%04X", writeRequest.getReference(), writeRequest.getRegister().getValue());
            }
            int pduLength = pduHex.length() / 2;
            return readResponse(String.format("%04X0000%04X%02X", sentTransactionId.get() & 0xFFFF, pduLength + 1, SLAVE_ID) + pduHex);
        }).when(transport).readResponse();
    }

    /**
     * 连接真实网络上的从站（与 IotModbusTcpClientConnectionManager.createConnection 一致）
     */
    private ModbusConnection connect(int port) throws Exception {
        TCPMasterConnection networkTcpConnection = new TCPMasterConnection(InetAddress.getLoopbackAddress());
        networkTcpConnection.setPort(port);
        networkTcpConnection.setTimeout(500);
        networkTcpConnection.connect();
        ModbusConnection networkConnection = new ModbusConnection();
        networkConnection.setTcpConnection(networkTcpConnection);
        networkConnection.setContext(vertx.getOrCreateContext());
        return networkConnection;
    }

    /**
     * 位点位：保持寄存器 0x0010、BOOLEAN、1 个寄存器
     */
    private static IotModbusPointRespDTO randomBitPoint(Integer bitIndex) {
        IotModbusPointRespDTO point = new IotModbusPointRespDTO();
        point.setIdentifier("alarm");
        point.setFunctionCode(FC_READ_HOLDING_REGISTERS);
        point.setRegisterAddress(0x10);
        point.setRegisterCount(1);
        point.setRawDataType(IotModbusRawDataTypeEnum.BOOLEAN.getType());
        point.setBitIndex(bitIndex);
        return point;
    }

    private Future<byte[]> executeRaw() throws InterruptedException {
        IotModbusTx tx = IotModbusTx.ofClient(null, HexUtil.decodeHex(READ_PDU),
                System.nanoTime() + TimeUnit.SECONDS.toNanos(60), null);
        return await(IotModbusTcpClientUtils.executeRaw(connection, SLAVE_ID, tx));
    }

    /**
     * 等待 Future 完成（成功或失败）
     */
    private static <T> Future<T> await(Future<T> result) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        result.onComplete(ar -> latch.countDown());
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        return result;
    }

    /**
     * 与 ModbusTCPTransport.readResponse 相同：按功能码创建标准响应，再从完整 MBAP 帧读入
     */
    private static ModbusResponse readResponse(String hexFrame) throws IOException {
        byte[] frame = HexUtil.decodeHex(hexFrame);
        ModbusResponse response = ModbusResponse.createModbusResponse(frame[7] & 0xFF);
        response.readFrom(new DataInputStream(new ByteArrayInputStream(frame)));
        // ModbusTCPTransport.readResponse 读完后再设置一次事务号
        response.setTransactionID(((frame[0] & 0xFF) << 8) | (frame[1] & 0xFF));
        return response;
    }

    private static void assertFailedWith(Future<byte[]> result, Integer code) {
        assertTrue(result.failed());
        assertInstanceOf(ServiceException.class, result.cause());
        assertEquals(code, ((ServiceException) result.cause()).getCode());
    }

    /**
     * 真实网络上的 Modbus TCP 从站：只有 1 个保持寄存器，处理 FC03 读、FC06 写；
     * writeAckDelayMillis 大于 0 时，FC06 先写入寄存器，延迟后再回显（模拟回显晚于网关的读超时）。
     * 每个连接一个线程，网关关闭连接后可以重新连上
     */
    private static class FakeRegisterSlave implements AutoCloseable {

        private final ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        private final AtomicInteger register;
        private volatile long writeAckDelayMillis;
        /**
         * 收到的 FC06 次数
         */
        private final AtomicInteger writeCount = new AtomicInteger();
        /**
         * FC06 的回显已发出（连接已被网关关闭、发送失败也算）
         */
        private final CountDownLatch writeAcked = new CountDownLatch(1);

        private FakeRegisterSlave(int register, long writeAckDelayMillis) throws IOException {
            this.register = new AtomicInteger(register);
            this.writeAckDelayMillis = writeAckDelayMillis;
            Thread thread = new Thread(this::accept, "fake-modbus-slave");
            thread.setDaemon(true);
            thread.start();
        }

        private void accept() {
            try {
                while (true) {
                    Socket socket = serverSocket.accept();
                    Thread thread = new Thread(() -> serve(socket), "fake-modbus-slave-connection");
                    thread.setDaemon(true);
                    thread.start();
                }
            } catch (IOException ignored) {
                // 测试结束，serverSocket 关闭
            }
        }

        private void serve(Socket socket) {
            try (Socket closeable = socket;
                 DataInputStream in = new DataInputStream(closeable.getInputStream());
                 DataOutputStream out = new DataOutputStream(closeable.getOutputStream())) {
                while (true) {
                    // 1. 读请求：MBAP 头（事务号、协议号、长度、unit）+ PDU
                    int transactionId = in.readUnsignedShort();
                    in.readUnsignedShort();
                    byte[] pdu = new byte[in.readUnsignedShort() - 1];
                    int unitId = in.readUnsignedByte();
                    in.readFully(pdu);
                    // 2. 处理请求：FC03 返回寄存器值；FC06 写入寄存器后原样回显（可延迟）
                    byte[] response;
                    if (pdu[0] == 3) {
                        int value = register.get();
                        response = new byte[]{3, 2, (byte) (value >> 8), (byte) value};
                    } else {
                        writeCount.incrementAndGet();
                        register.set(((pdu[3] & 0xFF) << 8) | (pdu[4] & 0xFF));
                        if (writeAckDelayMillis > 0) {
                            Thread.sleep(writeAckDelayMillis);
                        }
                        response = pdu;
                    }
                    // 3. 写响应
                    try {
                        out.writeShort(transactionId);
                        out.writeShort(0);
                        out.writeShort(response.length + 1);
                        out.writeByte(unitId);
                        out.write(response);
                        out.flush();
                    } finally {
                        if (pdu[0] == 6) {
                            writeAcked.countDown();
                        }
                    }
                }
            } catch (IOException | InterruptedException ignored) {
                // 测试结束或网关关闭连接
            }
        }

        public boolean awaitWriteAck() throws InterruptedException {
            return writeAcked.await(5, TimeUnit.SECONDS);
        }

        public void setWriteAckDelayMillis(long writeAckDelayMillis) {
            this.writeAckDelayMillis = writeAckDelayMillis;
        }

        public int getPort() {
            return serverSocket.getLocalPort();
        }

        public int getRegister() {
            return register.get();
        }

        public void setRegister(int value) {
            register.set(value);
        }

        public int getWriteCount() {
            return writeCount.get();
        }

        @Override
        public void close() throws IOException {
            serverSocket.close();
        }

    }

}
