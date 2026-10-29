package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.handler.downstream;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.IotModbusTcpClientProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConnectionManager.ModbusConnection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotModbusTcpClientDownstreamSubscriber} 的单元测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
public class IotModbusTcpClientDownstreamSubscriberTest {

    private static final Long DEVICE_ID = 1L;
    /**
     * 03 00 10 00 02：读 2 个保持寄存器
     */
    private static final byte[] READ_PDU = {0x03, 0x00, 0x10, 0x00, 0x02};

    @Mock
    private IotModbusTcpClientProtocol protocol;
    @Mock
    private IotModbusTcpClientDownstreamHandler downstreamHandler;
    @Mock
    private IotModbusTcpClientConnectionManager connectionManager;
    @Mock
    private IotMessageBus messageBus;

    private IotModbusTcpClientDownstreamSubscriber subscriber;

    @BeforeEach
    public void setUp() {
        subscriber = new IotModbusTcpClientDownstreamSubscriber(protocol, downstreamHandler, connectionManager, messageBus);
    }

    @Test
    public void testHandleRawMessage_pduInvalid() {
        // 准备参数：开头多带了 slaveId 的 PDU（01 06 00 10 00 DC）
        byte[] pdu = {0x01, 0x06, 0x00, 0x10, 0x00, (byte) 0xDC};

        // 调用
        Future<Void> result = subscriber.handleRawMessage(buildMessage(), pdu, null);

        // 断言
        assertFailedWith(result, MODBUS_PDU_INVALID.getCode());
    }

    @Test
    public void testHandleRawMessage_notConnected() {
        // mock 方法
        when(connectionManager.getConnection(DEVICE_ID)).thenReturn(null);

        // 调用
        Future<Void> result = subscriber.handleRawMessage(buildMessage(), READ_PDU, null);

        // 断言
        assertFailedWith(result, DEVICE_OFFLINE_OR_WRITE_FAILED.getCode());
    }

    @Test
    public void testHandleRawMessage_rawSlotFull() {
        // 准备参数：同一连接已有 32 个 raw 在途或排队
        ModbusConnection connection = new ModbusConnection();
        connection.addDevice(DEVICE_ID, 1);
        for (int i = 0; i < IotModbusTcpClientDownstreamSubscriber.MAX_RAW_OUTSTANDING; i++) {
            assertTrue(connection.tryAcquireRawSlot(IotModbusTcpClientDownstreamSubscriber.MAX_RAW_OUTSTANDING));
        }
        // mock 方法
        when(connectionManager.getConnection(DEVICE_ID)).thenReturn(connection);

        // 调用：第 33 个
        Future<Void> result = subscriber.handleRawMessage(buildMessage(), READ_PDU, null);

        // 断言：立即 113，不进入 executeBlocking（connection 没有 Context），名额不泄漏
        assertFailedWith(result, MODBUS_QUEUE_FULL.getCode());
        assertEquals(IotModbusTcpClientDownstreamSubscriber.MAX_RAW_OUTSTANDING, connection.getRawOutstanding().get());
        // 释放一个后可以再次占用
        connection.releaseRawSlot();
        assertTrue(connection.tryAcquireRawSlot(IotModbusTcpClientDownstreamSubscriber.MAX_RAW_OUTSTANDING));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testHandleRawMessage_submitRejected() {
        // 准备参数：连接的 Context 已关闭，提交 executeBlocking 时同步拒绝
        Context context = mock(Context.class);
        ModbusConnection connection = new ModbusConnection().setTimeout(1000).setContext(context);
        connection.addDevice(DEVICE_ID, 1);
        // mock 方法
        when(connectionManager.getConnection(DEVICE_ID)).thenReturn(connection);
        when(context.executeBlocking(any(Callable.class), eq(true)))
                .thenThrow(new RejectedExecutionException("context closed"));

        // 调用：连续 2 次
        for (int i = 0; i < 2; i++) {
            Future<Void> result = subscriber.handleRawMessage(buildMessage(), READ_PDU, null);

            // 断言：返回失败 Future（由调用方记日志），名额只释放一次，不泄漏、不变负
            assertFailedWith(result, DEVICE_OFFLINE_OR_WRITE_FAILED.getCode());
            assertEquals(0, connection.getRawOutstanding().get());
        }
        // 断言：没有进入事务，不回复 biz
        verify(context, times(2)).executeBlocking(any(Callable.class), eq(true));
        verify(protocol, never()).getVertx();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testHandleRawMessage_onCompleteRejected() {
        // 准备参数：提交成功，但注册完成回调时同步拒绝（Context 已关闭，无法把回调投递到 owner）
        Context context = mock(Context.class);
        ModbusConnection connection = new ModbusConnection().setTimeout(1000).setContext(context);
        connection.addDevice(DEVICE_ID, 1);
        Future<byte[]> execute = mock(Future.class);
        // mock 方法
        when(connectionManager.getConnection(DEVICE_ID)).thenReturn(connection);
        doReturn(execute).when(context).executeBlocking(any(Callable.class), eq(true));
        when(execute.onComplete(any(Handler.class))).thenThrow(new RejectedExecutionException("event executor terminated"));

        // 调用：连续 2 次
        for (int i = 0; i < 2; i++) {
            Future<Void> result = subscriber.handleRawMessage(buildMessage(), READ_PDU, null);

            // 断言：返回失败 Future，名额释放，不泄漏、不变负
            assertFailedWith(result, DEVICE_OFFLINE_OR_WRITE_FAILED.getCode());
            assertEquals(0, connection.getRawOutstanding().get());
        }
        // 断言：不回复 biz
        verify(protocol, never()).getVertx();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testHandleRawMessage_onCompleteThrowsAfterCallback() {
        // 准备参数：注册完成回调时，回调已经执行（事务已是终态），之后才抛出异常
        Context context = mock(Context.class);
        ModbusConnection connection = new ModbusConnection().setTimeout(1000).setContext(context);
        connection.addDevice(DEVICE_ID, 1);
        Future<byte[]> execute = mock(Future.class);
        // mock 方法
        when(connectionManager.getConnection(DEVICE_ID)).thenReturn(connection);
        doReturn(execute).when(context).executeBlocking(any(Callable.class), eq(true));
        doAnswer(invocation -> {
            Handler<AsyncResult<byte[]>> handler = invocation.getArgument(0);
            handler.handle(Future.failedFuture(new RuntimeException("Connection reset")));
            throw new RejectedExecutionException("event executor terminated");
        }).when(execute).onComplete(any(Handler.class));

        // 调用
        Future<Void> result = subscriber.handleRawMessage(buildMessage(), READ_PDU, null);

        // 断言：事务终态时已成功完成 Future（网关判定的失败只记日志），之后的异常不改变它；名额只释放一次，不变负
        assertTrue(result.succeeded());
        assertEquals(0, connection.getRawOutstanding().get());
        // 断言：不回复 biz
        verify(protocol, never()).getVertx();
    }

    private static IotDeviceMessage buildMessage() {
        return new IotDeviceMessage().setDeviceId(DEVICE_ID).setRequestId("r1").setMethod("thing.raw.push");
    }

    private static void assertFailedWith(Future<Void> result, Integer code) {
        assertTrue(result.failed());
        assertInstanceOf(ServiceException.class, result.cause());
        assertEquals(code, ((ServiceException) result.cause()).getCode());
    }

}
