package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.handler.downstream;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptSource;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager.IotModbusTx;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.IotModbusTcpServerProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager.ConnectionInfo;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerTransactionChannel;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.concurrent.Callable;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.MODBUS_EXCEPTION;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.MODBUS_TIMEOUT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotModbusTcpServerDownstreamSubscriber} 的单元测试
 *
 * @author 芋道源码
 */
public class IotModbusTcpServerDownstreamSubscriberTest extends BaseMockitoUnitTest {

    private static final Long DEVICE_ID = 1L;
    private static final String SERVER_ID = "server-1";
    /**
     * 06 00 10 00 01：写单个寄存器
     */
    private static final byte[] WRITE_PDU = {0x06, 0x00, 0x10, 0x00, 0x01};

    @Mock
    private IotModbusTcpServerProtocol protocol;
    @Mock
    private IotModbusTcpServerDownstreamHandler downstreamHandler;
    @Mock
    private IotModbusTcpServerConnectionManager connectionManager;
    @Mock
    private IotMessageBus messageBus;
    @Mock
    private IotModbusTcpServerTransactionChannel channel;
    @Mock
    private Vertx vertx;
    @Mock
    private IotDeviceMessageService deviceMessageService;

    private MockedStatic<SpringUtil> springUtilMockedStatic;
    private IotModbusTcpServerDownstreamSubscriber subscriber;

    private final IotScriptSource source = IotScriptSource.of(100L, "function decode(ctx) {}");

    @BeforeEach
    public void setUp() {
        springUtilMockedStatic = mockStatic(SpringUtil.class);
        subscriber = new IotModbusTcpServerDownstreamSubscriber(protocol, downstreamHandler, connectionManager, messageBus);
        when(connectionManager.getConnectionInfoByDeviceId(DEVICE_ID)).thenReturn(new ConnectionInfo().setChannel(channel));
    }

    @AfterEach
    public void tearDown() {
        springUtilMockedStatic.close();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testHandleRawMessage_deviceExceptionResponse() {
        // 准备参数
        IotDeviceMessage message = buildMessage();
        byte[] responsePdu = new byte[]{(byte) 0x86, 0x02};
        // mock 方法：回复在 worker 上执行（这里在当前线程同步执行）
        when(protocol.getServerId()).thenReturn(SERVER_ID);
        when(protocol.getVertx()).thenReturn(vertx);
        when(vertx.executeBlocking(any(Callable.class), eq(false))).thenAnswer(invocation -> {
            Callable<Object> callable = invocation.getArgument(0);
            return Future.succeededFuture(callable.call());
        });
        springUtilMockedStatic.when(() -> SpringUtil.getBean(IotDeviceMessageService.class)).thenReturn(deviceMessageService);

        // 调用：提交事务，事务终态之前 Future 不完成
        Future<Void> result = subscriber.handleRawMessage(message, WRITE_PDU, source);
        assertFalse(result.isComplete());
        // 调用：设备返回异常响应 86 02
        captureSubmittedTx().complete(responsePdu);

        // 断言：Future 在事务终态成功完成
        assertTrue(result.succeeded());
        // 断言：设备的实际响应回复给 biz，设备错误为 MODBUS_EXCEPTION（带异常码），带上 encode 时固定的脚本
        ArgumentCaptor<ErrorCode> errorCaptor = ArgumentCaptor.forClass(ErrorCode.class);
        verify(deviceMessageService).sendRawReply(same(message), same(source), aryEq(responsePdu),
                errorCaptor.capture(), eq(SERVER_ID));
        assertEquals(MODBUS_EXCEPTION.getCode(), errorCaptor.getValue().getCode());
        assertEquals(StrUtil.format(MODBUS_EXCEPTION.getMsg(), 2), errorCaptor.getValue().getMsg());
    }

    @Test
    public void testHandleRawMessage_gatewayFailureOnlyLog() {
        // 调用：网关判定的失败（超时）、响应为空
        Future<Void> timeout = subscriber.handleRawMessage(buildMessage(), WRITE_PDU, source);
        captureSubmittedTx().fail(MODBUS_TIMEOUT);
        Future<Void> empty = subscriber.handleRawMessage(buildMessage(), WRITE_PDU, null);
        captureSubmittedTx().complete(new byte[0]);

        // 断言：Future 照常在事务终态成功完成；只记日志，不给 biz 构造回复
        assertTrue(timeout.succeeded());
        assertTrue(empty.succeeded());
        verify(protocol, never()).getVertx();
        springUtilMockedStatic.verifyNoInteractions();
        verifyNoInteractions(deviceMessageService);
    }

    private IotModbusTx captureSubmittedTx() {
        ArgumentCaptor<IotModbusTx> captor = ArgumentCaptor.forClass(IotModbusTx.class);
        verify(channel, atLeastOnce()).submitRaw(captor.capture());
        return captor.getValue();
    }

    private static IotDeviceMessage buildMessage() {
        return new IotDeviceMessage().setDeviceId(DEVICE_ID).setRequestId("r1").setMethod("thing.property.set");
    }

}
