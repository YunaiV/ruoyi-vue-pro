package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager;

import cn.hutool.core.util.HexUtil;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager.IotModbusTx;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrame;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerPendingRequestManager.PendingRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link IotModbusTcpServerPendingRequestManager} 的单元测试
 *
 * @author 芋道源码
 */
public class IotModbusTcpServerPendingRequestManagerTest {

    private static final Long DEVICE_ID = 1L;
    private static final int SLAVE_ID = 1;

    private final IotModbusTcpServerPendingRequestManager pendingRequestManager = new IotModbusTcpServerPendingRequestManager();

    /**
     * 旧连接 A 被新连接 B 替换
     */
    private final IotModbusTcpServerTransactionChannel channelA = mock(IotModbusTcpServerTransactionChannel.class);
    private final IotModbusTcpServerTransactionChannel channelB = mock(IotModbusTcpServerTransactionChannel.class);

    // ========== TCP 帧：按 transactionId ==========

    @Test
    public void testMatchResponse_tcpOnlyMatchCurrentChannel() {
        // 准备参数：A、B 两条连接上各有一个事务号相同的轮询请求
        PendingRequest requestA = buildPollRequest(channelA, 5, 3, 2, System.currentTimeMillis() + 60_000);
        PendingRequest requestB = buildPollRequest(channelB, 5, 3, 2, System.currentTimeMillis() + 60_000);
        pendingRequestManager.addRequest(requestA);
        pendingRequestManager.addRequest(requestB);

        // 调用：B 收到响应
        PendingRequest matched = pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(5, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_TCP, channelB);

        // 断言：只配给 B 的请求，A 的请求不被取走
        assertSame(requestB, matched);
        assertSame(requestA, pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(5, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_TCP, channelA));
    }

    @Test
    public void testMatchResponse_tcpRawUnitOrFunctionCodeMismatch() {
        // 准备参数
        PendingRequest request = buildRawRequest(channelA, 5, 3, 2, System.currentTimeMillis() + 60_000);
        pendingRequestManager.addRequest(request);

        // 调用 + 断言：事务号相同，但功能码或 unit 不同：不出队
        assertNull(pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(5, 4, 2),
                IotModbusFrameFormatEnum.MODBUS_TCP, channelA));
        assertNull(pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(5, 3, 2).setSlaveId(SLAVE_ID + 1),
                IotModbusFrameFormatEnum.MODBUS_TCP, channelA));
        assertSame(request, pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(5, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_TCP, channelA));
    }

    @Test
    public void testMatchResponse_tcpExceptionOnlyMatchRaw() {
        // 准备参数：轮询请求 5 号、透传请求 6 号
        PendingRequest poll = buildPollRequest(channelA, 5, 3, 2, System.currentTimeMillis() + 60_000);
        PendingRequest raw = buildRawRequest(channelA, 6, 3, 2, System.currentTimeMillis() + 60_000);
        pendingRequestManager.addRequest(poll);
        pendingRequestManager.addRequest(raw);

        // 调用 + 断言：轮询的异常响应照旧丢弃，不出队
        assertNull(pendingRequestManager.matchResponse(DEVICE_ID, buildExceptionFrame(5, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_TCP, channelA));
        // 调用 + 断言：透传的异常响应配给透传请求
        assertSame(raw, pendingRequestManager.matchResponse(DEVICE_ID, buildExceptionFrame(6, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_TCP, channelA));
        // 断言：轮询请求仍在，留给到期清理
        assertTrue(pendingRequestManager.containsTransactionId(DEVICE_ID, channelA, 5));
    }

    // ========== RTU 帧：原有的 FIFO（slaveId + functionCode + registerCount） ==========

    @Test
    public void testMatchResponse_rtuFifoBySlaveFunctionCodeCount() {
        // 准备参数：同功能码、数量不同的两个轮询请求
        PendingRequest request2 = buildPollRequest(channelA, null, 3, 2, System.currentTimeMillis() + 60_000);
        PendingRequest request1 = buildPollRequest(channelA, null, 3, 1, System.currentTimeMillis() + 60_000);
        pendingRequestManager.addRequest(request2);
        pendingRequestManager.addRequest(request1);

        // 调用 + 断言：unit 不同不匹配
        assertNull(pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(null, 3, 1).setSlaveId(SLAVE_ID + 1),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA));
        // 调用 + 断言：按数量配给对应的请求，与先后顺序无关
        assertSame(request1, pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(null, 3, 1),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA));
        assertSame(request2, pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(null, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA));
    }

    /**
     * 原有逻辑的限制（不是安全保证）：RTU 帧没有事务号，同 unit、同功能码、同数量的请求无法区分，响应按 FIFO 配给排在前面的请求
     */
    @Test
    public void testMatchResponse_rtuSameShapeMatchFirst_originalLimit() {
        // 准备参数：轮询请求 P 在前，同形状的透传请求 R 在后
        PendingRequest poll = buildPollRequest(channelA, null, 3, 2, System.currentTimeMillis() + 60_000);
        PendingRequest raw = buildRawRequest(channelA, null, 3, 2, System.currentTimeMillis() + 60_000);
        pendingRequestManager.addRequest(poll);
        pendingRequestManager.addRequest(raw);

        // 调用：无论这个响应实际是谁的，都配给排在前面的 P
        PendingRequest matched = pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(null, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA);

        // 断言：下一个同形状的正常响应才配给 R
        assertSame(poll, matched);
        assertSame(raw, pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(null, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA));
    }

    @Test
    public void testMatchResponse_rtuRawThenPollNormal() {
        // 准备参数：透传请求 R 在前，同形状的轮询请求 P 在后
        PendingRequest raw = buildRawRequest(channelA, null, 3, 2, System.currentTimeMillis() + 60_000);
        PendingRequest poll = buildPollRequest(channelA, null, 3, 2, System.currentTimeMillis() + 60_000);
        pendingRequestManager.addRequest(raw);
        pendingRequestManager.addRequest(poll);

        // 调用
        PendingRequest matched = pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(null, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA);

        // 断言：按 FIFO 先配给 R，下一个再配给 P
        assertSame(raw, matched);
        assertSame(poll, pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(null, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA));
    }

    @Test
    public void testMatchResponse_rtuSkipOldConnection() {
        // 准备参数：旧连接 A 的请求排在前面，新连接 B 发出同 unit、同功能码、同数量的请求
        PendingRequest requestA = buildPollRequest(channelA, null, 3, 2, System.currentTimeMillis() + 60_000);
        PendingRequest requestB = buildPollRequest(channelB, null, 3, 2, System.currentTimeMillis() + 60_000);
        pendingRequestManager.addRequest(requestA);
        pendingRequestManager.addRequest(requestB);

        // 调用：B 收到响应
        PendingRequest matched = pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(null, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelB);

        // 断言：响应配给 B 的请求，不是 A 的旧条目
        assertSame(requestB, matched);
    }

    @Test
    public void testMatchResponse_rtuPollThenRawException() {
        // 准备参数：FC03，轮询请求 P（读 1 个）在前，透传请求 R（读 2 个）在后
        PendingRequest poll = buildPollRequest(channelA, null, 3, 1, System.currentTimeMillis() + 60_000);
        PendingRequest raw = buildRawRequest(channelA, null, 3, 2, System.currentTimeMillis() + 60_000);
        pendingRequestManager.addRequest(poll);
        pendingRequestManager.addRequest(raw);

        // 调用：设备回 83 02（解码器已还原原功能码，pdu[0] 是异常码）
        PendingRequest matched = pendingRequestManager.matchResponse(DEVICE_ID, buildExceptionFrame(null, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA);

        // 断言：按 FIFO 属于排在前面的 P：照旧丢弃，不跳过 P 去收尾 R；P、R 都不出队
        assertNull(matched);
        assertSame(poll, pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(null, 3, 1),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA));
        // 调用 + 断言：P 出队后 R 排在最前面，下一个异常响应收尾 R
        assertSame(raw, pendingRequestManager.matchResponse(DEVICE_ID, buildExceptionFrame(null, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA));
    }

    @Test
    public void testMatchResponse_rtuRawThenPollException() {
        // 准备参数：FC03，透传请求 R 在前，轮询请求 P 在后
        PendingRequest raw = buildRawRequest(channelA, null, 3, 2, System.currentTimeMillis() + 60_000);
        PendingRequest poll = buildPollRequest(channelA, null, 3, 2, System.currentTimeMillis() + 60_000);
        pendingRequestManager.addRequest(raw);
        pendingRequestManager.addRequest(poll);

        // 调用：设备回 83 02
        PendingRequest matched = pendingRequestManager.matchResponse(DEVICE_ID, buildExceptionFrame(null, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA);

        // 断言：R 在最前面，由异常响应收尾；P 不被消费
        assertSame(raw, matched);
        assertSame(poll, pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(null, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA));
    }

    @Test
    public void testMatchResponse_rtuExceptionIgnoreOtherUnitOrFunctionCode() {
        // 准备参数：排在前面的轮询请求是其它 unit、其它功能码，透传请求 R 在后
        PendingRequest otherUnit = new PendingRequest(DEVICE_ID, 10L, "temperature", SLAVE_ID + 1, 3, 0, 2,
                null, System.currentTimeMillis() + 60_000, channelA, null);
        PendingRequest otherFunctionCode = buildPollRequest(channelA, null, 4, 2, System.currentTimeMillis() + 60_000);
        PendingRequest raw = buildRawRequest(channelA, null, 3, 2, System.currentTimeMillis() + 60_000);
        pendingRequestManager.addRequest(otherUnit);
        pendingRequestManager.addRequest(otherFunctionCode);
        pendingRequestManager.addRequest(raw);

        // 调用：设备回 83 02
        PendingRequest matched = pendingRequestManager.matchResponse(DEVICE_ID, buildExceptionFrame(null, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA);

        // 断言：只按同 unit、同功能码排队，R 是其中最前面的
        assertSame(raw, matched);
    }

    @Test
    public void testMatchResponse_rtuExceptionWithoutRawDrop() {
        // 准备参数：只有轮询请求
        PendingRequest poll = buildPollRequest(channelA, null, 3, 2, System.currentTimeMillis() + 60_000);
        pendingRequestManager.addRequest(poll);

        // 调用：轮询的异常响应
        PendingRequest matched = pendingRequestManager.matchResponse(DEVICE_ID, buildExceptionFrame(null, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA);

        // 断言：照旧丢弃，轮询请求留给到期清理
        assertNull(matched);
        assertSame(poll, pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(null, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA));
    }

    // ========== 清理 ==========

    @Test
    public void testCleanupExpired() {
        // 准备参数
        PendingRequest expired = buildPollRequest(channelA, 1, 3, 2, System.currentTimeMillis() - 1);
        PendingRequest alive = buildPollRequest(channelA, 2, 3, 2, System.currentTimeMillis() + 60_000);
        pendingRequestManager.addRequest(expired);
        pendingRequestManager.addRequest(alive);

        // 调用
        pendingRequestManager.cleanupExpired();

        // 断言：只移除到期的条目
        assertFalse(pendingRequestManager.containsTransactionId(DEVICE_ID, channelA, 1));
        assertTrue(pendingRequestManager.containsTransactionId(DEVICE_ID, channelA, 2));
    }

    @Test
    public void testRemoveChannel_keepNewChannel() {
        // 准备参数：旧连接 A、新连接 B 各有一个条目
        pendingRequestManager.addRequest(buildPollRequest(channelA, 1, 3, 2, System.currentTimeMillis() + 60_000));
        pendingRequestManager.addRequest(buildRawRequest(channelB, 2, 3, 2, System.currentTimeMillis() + 60_000));

        // 调用：旧连接关闭
        pendingRequestManager.removeChannel(DEVICE_ID, channelA);

        // 断言：只删 A 的条目，B 的条目仍可匹配
        assertFalse(pendingRequestManager.containsTransactionId(DEVICE_ID, channelA, 1));
        assertTrue(pendingRequestManager.containsTransactionId(DEVICE_ID, channelB, 2));
    }

    @Test
    public void testRemoveRequest_byReference() {
        // 准备参数：两个字段完全相同的条目（PendingRequest 的 equals 按字段比较）
        long expireAt = System.currentTimeMillis() + 60_000;
        PendingRequest request1 = buildPollRequest(channelA, null, 3, 2, expireAt);
        PendingRequest request2 = buildPollRequest(channelA, null, 3, 2, expireAt);
        pendingRequestManager.addRequest(request1);
        pendingRequestManager.addRequest(request2);

        // 调用
        pendingRequestManager.removeRequest(request1);

        // 断言：只移除 request1 这个引用
        assertSame(request2, pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(null, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA));
        assertNull(pendingRequestManager.matchResponse(DEVICE_ID, buildReadFrame(null, 3, 2),
                IotModbusFrameFormatEnum.MODBUS_RTU, channelA));
    }

    // ========== 辅助方法 ==========

    private static PendingRequest buildPollRequest(IotModbusTcpServerTransactionChannel channel, Integer transactionId,
                                                   int functionCode, int registerCount, long expireAt) {
        return new PendingRequest(DEVICE_ID, 10L, "temperature", SLAVE_ID, functionCode, 0, registerCount,
                transactionId, expireAt, channel, null);
    }

    private static PendingRequest buildRawRequest(IotModbusTcpServerTransactionChannel channel, Integer transactionId,
                                                  int functionCode, int registerCount, long expireAt) {
        when(channel.getRequestTimeout()).thenReturn(60_000);
        byte[] pdu = HexUtil.decodeHex(String.format("%02X0000%04X", functionCode, registerCount));
        IotModbusTx tx = IotModbusTx.ofServer(channel, pdu, null, null);
        return new PendingRequest(DEVICE_ID, null, null, SLAVE_ID, functionCode, 0, registerCount,
                transactionId, expireAt, channel, tx);
    }

    private static IotModbusFrame buildReadFrame(Integer transactionId, int functionCode, int registerCount) {
        byte[] pdu = new byte[1 + registerCount * 2];
        pdu[0] = (byte) (registerCount * 2);
        return new IotModbusFrame().setSlaveId(SLAVE_ID).setFunctionCode(functionCode)
                .setPdu(pdu).setTransactionId(transactionId);
    }

    /**
     * 异常响应帧：解码器已还原原功能码，pdu[0] 是异常码
     */
    private static IotModbusFrame buildExceptionFrame(Integer transactionId, int functionCode, int exceptionCode) {
        return new IotModbusFrame().setSlaveId(SLAVE_ID).setFunctionCode(functionCode)
                .setPdu(new byte[]{(byte) exceptionCode}).setExceptionCode(exceptionCode)
                .setTransactionId(transactionId);
    }

}
