package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.handler.upstream.IotModbusTcpClientUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConnectionManager.ModbusConnection;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientPollScheduler.ReadSegment;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotWorkerTaskQueue;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils.FC_READ_COILS;
import static cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils.FC_READ_HOLDING_REGISTERS;
import static cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils.FC_READ_INPUT_REGISTERS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotModbusTcpClientPollScheduler} 的单元测试
 *
 * @author 芋道源码
 */
public class IotModbusTcpClientPollSchedulerTest extends BaseMockitoUnitTest {

    private static final long TIMEOUT_SECONDS = 10;
    private static final Long DEVICE_ID = 1L;
    private static final Long POINT_ID = 1L;
    private static final int SLAVE_ID = 1;
    private static final String PRODUCT_KEY = "pk";
    private static final String DEVICE_NAME = "dn";
    private static final String SERVER_ID = "modbus-tcp-client";

    @Mock
    private IotModbusTcpClientConnectionManager connectionManager;
    @Mock
    private IotModbusTcpClientConfigCacheService configCacheService;
    @Mock
    private IotDeviceMessageService messageService;
    @Mock
    private ModbusConnection connection;
    @Mock
    private IotWorkerTaskQueue upstreamQueue;

    @Test
    public void testPollPoint_reportNotBlockConnectionContext() throws Exception {
        Vertx vertx = Vertx.vertx();
        try {
            // 准备参数：连接的 context 与它上面的上报队列；点位 FC03 读 1 个寄存器
            Context context = vertx.getOrCreateContext();
            IotWorkerTaskQueue queue = new IotWorkerTaskQueue(context);
            IotModbusPointRespDTO point = randomPoint(POINT_ID, FC_READ_HOLDING_REGISTERS, 0, 1, 1000);
            point.setIdentifier("temperature");
            point.setRawDataType("INT16");
            IotModbusDeviceConfigRespDTO config = randomConfig(point);
            config.setProductKey(PRODUCT_KEY);
            config.setDeviceName(DEVICE_NAME);
            IotModbusTcpClientPollScheduler pollScheduler = new IotModbusTcpClientPollScheduler(vertx, connectionManager,
                    new IotModbusTcpClientUpstreamHandler(messageService, SERVER_ID), configCacheService);
            CountDownLatch entered = new CountDownLatch(1);
            CountDownLatch release = new CountDownLatch(1);
            CountDownLatch delivered = new CountDownLatch(2);
            AtomicBoolean onWorker = new AtomicBoolean(true);
            // mock 方法：配置、连接、slave ID
            when(configCacheService.getConfig(DEVICE_ID)).thenReturn(config);
            when(connectionManager.getConnection(DEVICE_ID)).thenReturn(connection);
            when(connectionManager.getSlaveId(DEVICE_ID)).thenReturn(SLAVE_ID);
            when(connection.getUpstreamQueue()).thenReturn(queue);
            // mock 方法：与真实连接一样，读取在连接 context 的有序 executeBlocking 上完成，结果回调回到 context；两次读到 250、251
            when(connection.executeBlocking(any())).thenAnswer(invocation -> context.executeBlocking(() -> new int[]{250}, true))
                    .thenAnswer(invocation -> context.executeBlocking(() -> new int[]{251}, true));
            // mock 方法：发送到消息总线，第一条阻塞直到放行
            doAnswer(invocation -> {
                onWorker.compareAndSet(true, Context.isOnWorkerThread());
                entered.countDown();
                if (!release.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("等待放行超时");
                }
                delivered.countDown();
                return null;
            }).doAnswer(invocation -> {
                onWorker.compareAndSet(true, Context.isOnWorkerThread());
                delivered.countDown();
                return null;
            }).when(messageService).sendDeviceMessage(any(IotDeviceMessage.class), eq(PRODUCT_KEY), eq(DEVICE_NAME), eq(SERVER_ID));

            try {
                // 调用：第一次轮询，发送阻塞在 worker 上
                pollScheduler.pollPoint(DEVICE_ID, POINT_ID);
                assertTrue(entered.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
                // 调用：发送仍阻塞时，第二次轮询；再在连接的 context 上执行一个哨兵任务
                pollScheduler.pollPoint(DEVICE_ID, POINT_ID);
                CompletableFuture<Boolean> sentinel = new CompletableFuture<>();
                context.runOnContext(v -> sentinel.complete(true));

                // 断言：context 没有被发送阻塞，哨兵及时执行；第二条上报排在第一条之后，还没有发送
                assertTrue(sentinel.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
                verify(connection, timeout(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS)).times(2)).executeBlocking(any());
                verify(messageService, times(1)).sendDeviceMessage(any(), any(), any(), any());
            } finally {
                release.countDown();
            }
            // 断言：放行后两条上报按读取顺序各投递一次，都在 worker 上发送
            assertTrue(delivered.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
            verify(messageService, times(2)).sendDeviceMessage(captor.capture(), eq(PRODUCT_KEY), eq(DEVICE_NAME), eq(SERVER_ID));
            assertTrue(onWorker.get());
            List<IotDeviceMessage> messages = captor.getAllValues();
            assertEquals(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), messages.get(0).getMethod());
            assertEquals(Collections.singletonMap("temperature", IotModbusCommonUtils.convertToPropertyValue(new int[]{250}, point)),
                    messages.get(0).getParams());
            assertEquals(Collections.singletonMap("temperature", IotModbusCommonUtils.convertToPropertyValue(new int[]{251}, point)),
                    messages.get(1).getParams());
        } finally {
            vertx.close().toCompletionStage().toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testSubmitPollRequest_skipWhileInFlight() {
        // 准备参数：FC03 一个读取段，轮询间隔 100ms
        IotModbusPointRespDTO point = randomPoint(POINT_ID, FC_READ_HOLDING_REGISTERS, 0, 1, 100);
        IotModbusDeviceConfigRespDTO config = randomConfig(point);
        String taskKey = IotModbusTcpClientPollScheduler.buildReadSegments(config).get(0).getKey();
        IotModbusTcpClientPollScheduler pollScheduler = new IotModbusTcpClientPollScheduler(mock(Vertx.class), connectionManager,
                new IotModbusTcpClientUpstreamHandler(messageService, SERVER_ID), configCacheService);
        List<Promise<int[]>> reads = new ArrayList<>();
        // mock 方法：配置、连接、slave ID
        when(configCacheService.getConfig(DEVICE_ID)).thenReturn(config);
        when(connectionManager.getConnection(DEVICE_ID)).thenReturn(connection);
        when(connectionManager.getSlaveId(DEVICE_ID)).thenReturn(SLAVE_ID);
        // mock 方法：设备变慢，读取一直不结束，直到手动完成
        when(connection.executeBlocking(any())).thenAnswer(invocation -> {
            Promise<int[]> read = Promise.promise();
            reads.add(read);
            return read.future();
        });
        // mock 方法：上报队列
        when(connection.getUpstreamQueue()).thenReturn(upstreamQueue);
        when(upstreamQueue.submit(any())).thenReturn(Future.succeededFuture());

        // 调用：上次读取未结束时，又触发 2 次
        pollScheduler.submitPollRequest(DEVICE_ID, taskKey);
        pollScheduler.submitPollRequest(DEVICE_ID, taskKey);
        pollScheduler.submitPollRequest(DEVICE_ID, taskKey);
        // 断言：只发出 1 次读取，后 2 次跳过
        assertEquals(1, reads.size());

        // 调用：读取失败后再触发
        reads.get(0).fail("读取超时");
        pollScheduler.submitPollRequest(DEVICE_ID, taskKey);
        // 断言：失败释放在途标记，发出第 2 次读取
        assertEquals(2, reads.size());

        // 调用：读取成功后再触发
        reads.get(1).complete(new int[]{250});
        pollScheduler.submitPollRequest(DEVICE_ID, taskKey);
        // 断言：成功同样释放，发出第 3 次读取；成功的结果交给上报队列
        assertEquals(3, reads.size());
        verify(upstreamQueue, times(1)).submit(any());
    }

    @Test
    public void testSubmitPollRequest_releaseWhenReadThrows() {
        // 准备参数
        IotModbusPointRespDTO point = randomPoint(POINT_ID, FC_READ_HOLDING_REGISTERS, 0, 1, 100);
        IotModbusDeviceConfigRespDTO config = randomConfig(point);
        String taskKey = IotModbusTcpClientPollScheduler.buildReadSegments(config).get(0).getKey();
        IotModbusTcpClientPollScheduler pollScheduler = new IotModbusTcpClientPollScheduler(mock(Vertx.class), connectionManager,
                new IotModbusTcpClientUpstreamHandler(messageService, SERVER_ID), configCacheService);
        // mock 方法：配置、连接、slave ID
        when(configCacheService.getConfig(DEVICE_ID)).thenReturn(config);
        when(connectionManager.getConnection(DEVICE_ID)).thenReturn(connection);
        when(connectionManager.getSlaveId(DEVICE_ID)).thenReturn(SLAVE_ID);
        // mock 方法：第 1 次提交被同步拒绝（例如 Context 已关闭），第 2 次正常提交
        when(connection.executeBlocking(any())).thenThrow(new IllegalStateException("Context 已关闭"))
                .thenAnswer(invocation -> Promise.promise().future());

        // 调用 + 断言：同步异常照常抛出
        assertThrows(IllegalStateException.class, () -> pollScheduler.submitPollRequest(DEVICE_ID, taskKey));
        // 调用：再次触发
        pollScheduler.submitPollRequest(DEVICE_ID, taskKey);
        // 断言：在途标记已释放，第 2 次照常发出读取
        verify(connection, times(2)).executeBlocking(any());
    }

    @Test
    public void testSubmitPollRequest_noMinIntervalAndBoundInFlight() {
        // 准备参数：3 个不连续的读取段，轮询间隔都是 100ms
        IotModbusPointRespDTO point01 = randomPoint(1L, FC_READ_HOLDING_REGISTERS, 0, 1, 100);
        IotModbusPointRespDTO point02 = randomPoint(2L, FC_READ_HOLDING_REGISTERS, 10, 1, 100);
        IotModbusPointRespDTO point03 = randomPoint(3L, FC_READ_HOLDING_REGISTERS, 20, 1, 100);
        IotModbusDeviceConfigRespDTO config = randomConfig(point01, point02, point03);
        List<ReadSegment> segments = IotModbusTcpClientPollScheduler.buildReadSegments(config);
        assertEquals(3, segments.size());
        IotModbusTcpClientPollScheduler pollScheduler = new IotModbusTcpClientPollScheduler(mock(Vertx.class), connectionManager,
                new IotModbusTcpClientUpstreamHandler(messageService, SERVER_ID), configCacheService);
        List<Promise<int[]>> reads = new ArrayList<>();
        // mock 方法：配置、连接、slave ID
        when(configCacheService.getConfig(DEVICE_ID)).thenReturn(config);
        when(connectionManager.getConnection(DEVICE_ID)).thenReturn(connection);
        when(connectionManager.getSlaveId(DEVICE_ID)).thenReturn(SLAVE_ID);
        // mock 方法：设备变慢，读取一直不结束，直到手动完成
        when(connection.executeBlocking(any())).thenAnswer(invocation -> {
            Promise<int[]> read = Promise.promise();
            reads.add(read);
            return read.future();
        });
        // mock 方法：上报队列
        when(connection.getUpstreamQueue()).thenReturn(upstreamQueue);
        when(upstreamQueue.submit(any())).thenReturn(Future.succeededFuture());

        // 调用：每个读取段触发 1 次
        segments.forEach(segment -> pollScheduler.submitPollRequest(DEVICE_ID, segment.getKey()));
        // 断言：3 个读取段立即提交，不再经过基类的 1000ms 限速队列
        assertEquals(3, reads.size());

        // 调用：读取都未结束时，再触发 100 轮
        for (int i = 0; i < 100; i++) {
            segments.forEach(segment -> pollScheduler.submitPollRequest(DEVICE_ID, segment.getKey()));
        }
        // 断言：连接上排队的读取不超过读取段数，写入最多排在 3 个读取之后
        assertEquals(3, reads.size());

        // 调用：读取都结束后，再触发 1 轮
        reads.forEach(read -> read.complete(new int[]{0}));
        segments.forEach(segment -> pollScheduler.submitPollRequest(DEVICE_ID, segment.getKey()));
        // 断言：每个读取段各发出下一次读取
        assertEquals(6, reads.size());
    }

    @Test
    public void testBuildReadSegments_mergeContinuousPoints() {
        // 准备参数
        IotModbusPointRespDTO point01 = randomPoint(1L, FC_READ_HOLDING_REGISTERS, 0, 1, 1000);
        IotModbusPointRespDTO point02 = randomPoint(2L, FC_READ_HOLDING_REGISTERS, 1, 2, 1000);
        IotModbusPointRespDTO point03 = randomPoint(3L, FC_READ_HOLDING_REGISTERS, 4, 1, 1000);
        IotModbusDeviceConfigRespDTO config = randomConfig(point01, point02, point03);

        // 调用
        List<ReadSegment> segments = IotModbusTcpClientPollScheduler.buildReadSegments(config);

        // 断言
        assertEquals(2, segments.size());
        assertEquals(0, segments.get(0).getStartAddress());
        assertEquals(3, segments.get(0).getRegisterCount());
        assertIterableEquals(Arrays.asList(point01, point02), segments.get(0).getPoints());
        assertEquals(4, segments.get(1).getStartAddress());
        assertEquals(1, segments.get(1).getRegisterCount());
        assertIterableEquals(Collections.singletonList(point03), segments.get(1).getPoints());
    }

    @Test
    public void testBuildReadSegments_mergeOverlappingPoints() {
        // 准备参数
        IotModbusPointRespDTO point01 = randomPoint(1L, FC_READ_HOLDING_REGISTERS, 0, 2, 1000);
        IotModbusPointRespDTO point02 = randomPoint(2L, FC_READ_HOLDING_REGISTERS, 1, 1, 1000);
        IotModbusDeviceConfigRespDTO config = randomConfig(point01, point02);

        // 调用
        List<ReadSegment> segments = IotModbusTcpClientPollScheduler.buildReadSegments(config);

        // 断言
        assertEquals(1, segments.size());
        assertEquals(0, segments.get(0).getStartAddress());
        assertEquals(2, segments.get(0).getRegisterCount());
        assertIterableEquals(Arrays.asList(point01, point02), segments.get(0).getPoints());
    }

    @Test
    public void testBuildReadSegments_notMergeDifferentFunctionCodeOrPollInterval() {
        // 准备参数
        IotModbusPointRespDTO point01 = randomPoint(1L, FC_READ_HOLDING_REGISTERS, 0, 1, 1000);
        IotModbusPointRespDTO point02 = randomPoint(2L, FC_READ_INPUT_REGISTERS, 1, 1, 1000);
        IotModbusPointRespDTO point03 = randomPoint(3L, FC_READ_HOLDING_REGISTERS, 1, 1, 2000);
        IotModbusDeviceConfigRespDTO config = randomConfig(point01, point02, point03);

        // 调用
        List<ReadSegment> segments = IotModbusTcpClientPollScheduler.buildReadSegments(config);

        // 断言
        assertEquals(3, segments.size());
        assertEquals(FC_READ_HOLDING_REGISTERS, segments.get(0).getFunctionCode());
        assertEquals(1000, segments.get(0).getPollInterval());
        assertEquals(FC_READ_HOLDING_REGISTERS, segments.get(1).getFunctionCode());
        assertEquals(2000, segments.get(1).getPollInterval());
        assertEquals(FC_READ_INPUT_REGISTERS, segments.get(2).getFunctionCode());
        assertEquals(1000, segments.get(2).getPollInterval());
    }

    @Test
    public void testBuildReadSegments_splitWhenExceedsMaxRegisterCount() {
        // 准备参数
        IotModbusPointRespDTO point01 = randomPoint(1L, FC_READ_HOLDING_REGISTERS, 0, 100, 1000);
        IotModbusPointRespDTO point02 = randomPoint(2L, FC_READ_HOLDING_REGISTERS, 100, 30, 1000);
        IotModbusDeviceConfigRespDTO config = randomConfig(point01, point02);

        // 调用
        List<ReadSegment> segments = IotModbusTcpClientPollScheduler.buildReadSegments(config);

        // 断言
        assertEquals(2, segments.size());
        assertEquals(0, segments.get(0).getStartAddress());
        assertEquals(100, segments.get(0).getRegisterCount());
        assertEquals(100, segments.get(1).getStartAddress());
        assertEquals(30, segments.get(1).getRegisterCount());
    }

    @Test
    public void testExtractPointRawValues() {
        // 准备参数
        IotModbusPointRespDTO point = randomPoint(1L, FC_READ_HOLDING_REGISTERS, 12, 2, 1000);
        ReadSegment segment = new ReadSegment(FC_READ_HOLDING_REGISTERS, 1000, 10, 4, Collections.singletonList(point));
        int[] rawValues = new int[]{100, 200, 300, 400};

        // 调用
        int[] pointRawValues = IotModbusTcpClientPollScheduler.extractPointRawValues(rawValues, segment, point);

        // 断言
        assertArrayEquals(new int[]{300, 400}, pointRawValues);
    }

    @Test
    public void testExtractPointRawValuesForCoils() {
        // 准备参数
        IotModbusPointRespDTO point = randomPoint(1L, FC_READ_COILS, 3, 2, 1000);
        ReadSegment segment = new ReadSegment(FC_READ_COILS, 1000, 0, 5, Collections.singletonList(point));
        int[] rawValues = new int[]{1, 0, 1, 1, 0, 0, 0, 0}; // 线圈响应可能按字节补齐

        // 调用
        int[] pointRawValues = IotModbusTcpClientPollScheduler.extractPointRawValues(rawValues, segment, point);

        // 断言
        assertArrayEquals(new int[]{1, 0}, pointRawValues);
    }

    private static IotModbusDeviceConfigRespDTO randomConfig(IotModbusPointRespDTO... points) {
        IotModbusDeviceConfigRespDTO config = new IotModbusDeviceConfigRespDTO();
        config.setDeviceId(1L);
        config.setPoints(Arrays.asList(points));
        return config;
    }

    private static IotModbusPointRespDTO randomPoint(Long id, Integer functionCode, Integer registerAddress,
                                                     Integer registerCount, Integer pollInterval) {
        IotModbusPointRespDTO point = new IotModbusPointRespDTO();
        point.setId(id);
        point.setFunctionCode(functionCode);
        point.setRegisterAddress(registerAddress);
        point.setRegisterCount(registerCount);
        point.setPollInterval(pollInterval);
        return point;
    }

}
