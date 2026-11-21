package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.handler.downstream;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusByteOrderEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusRawDataTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertySetResultDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusTcpClientUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConnectionManager.ModbusConnection;
import com.ghgande.j2mod.modbus.ModbusSlaveException;
import io.vertx.core.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils.*;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

/**
 * {@link IotModbusTcpClientDownstreamHandler} 的单元测试
 * <p>
 * Modbus 读写由 {@link IotModbusTcpClientUtils} 执行（已有单独的单测），这里静态 mock 它的写入方法
 *
 * @author 芋道源码
 */
public class IotModbusTcpClientDownstreamHandlerTest extends BaseMockitoUnitTest {

    private static final Long DEVICE_ID = 1L;
    private static final int SLAVE_ID = 2;

    @InjectMocks
    private IotModbusTcpClientDownstreamHandler downstreamHandler;

    @Mock
    private IotModbusTcpClientConnectionManager connectionManager;
    @Mock
    private IotModbusTcpClientConfigCacheService configCacheService;

    private final ModbusConnection connection = new ModbusConnection();
    private MockedStatic<IotModbusTcpClientUtils> clientUtilsMock;

    @BeforeEach
    public void setUp() {
        // 只 mock 写入方法；toRawFailure 等走真实实现
        clientUtilsMock = mockStatic(IotModbusTcpClientUtils.class, CALLS_REAL_METHODS);
    }

    @AfterEach
    public void tearDown() {
        clientUtilsMock.close();
    }

    @Test
    public void testHandle_ignore() {
        // 调用，并断言：上行消息、服务调用、属性设置的回复都不处理，不回复
        assertNull(downstreamHandler.handle(randomMessage(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                singletonMap("temperature", 25))));
        assertNull(downstreamHandler.handle(randomMessage(IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod(),
                singletonMap("temperature", 25))));
        assertNull(downstreamHandler.handle(randomPropertySetMessage(singletonMap("temperature", 25)).setCode(0)));
        verifyNoInteractions(configCacheService, connectionManager);
    }

    @Test
    public void testHandle_paramsInvalid() {
        // 调用，并断言：params 不是 Map、空 Map，整条消息失败
        for (Object params : new Object[]{randomString(), new LinkedHashMap<>()}) {
            Future<Map<String, IotDevicePropertySetResultDTO>> future = downstreamHandler.handle(
                    randomMessage(IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod(), params));
            assertTrue(future.failed());
            assertEquals(PROPERTY_SET_PARAMS_INVALID.getCode(), ((ServiceException) future.cause()).getCode());
        }
        verifyNoInteractions(configCacheService, connectionManager);
    }

    @Test
    public void testHandle_configNotExists() {
        // mock 方法：无配置
        when(configCacheService.getConfig(DEVICE_ID)).thenReturn(null);

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(
                randomPropertySetMessage(singletonMap("temperature", 25))));
        // 断言：每个属性都失败，不查连接
        assertResult(results.get("temperature"), PROPERTY_SET_DEVICE_NOT_CONFIGURED);
        verifyNoInteractions(connectionManager);
    }

    @Test
    public void testHandle_notConnected() {
        // 准备参数
        IotModbusPointRespDTO point = randomPoint();
        // mock 方法：有配置、无连接
        mockConfig(point);
        when(connectionManager.getConnection(DEVICE_ID)).thenReturn(null);

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(
                randomPropertySetMessage(singletonMap(point.getIdentifier(), 25))));
        // 断言：设备离线，不写入
        assertResult(results.get(point.getIdentifier()), DEVICE_OFFLINE_OR_WRITE_FAILED);
        clientUtilsMock.verifyNoInteractions();
    }

    @Test
    public void testHandle_partialFailure() {
        // 准备参数：unknown 没有点位；humidity 只读（FC04）；pressure 超出 INT16 范围；mode 值不是数字；temperature 正常
        IotModbusPointRespDTO inputPoint = randomPoint(o -> o.setIdentifier("humidity").setFunctionCode(FC_READ_INPUT_REGISTERS));
        IotModbusPointRespDTO pressurePoint = randomPoint(o -> o.setIdentifier("pressure"));
        IotModbusPointRespDTO modePoint = randomPoint(o -> o.setIdentifier("mode"));
        IotModbusPointRespDTO point = randomPoint(o -> o.setIdentifier("temperature").setScale(new BigDecimal("0.1")));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("unknown", 1);
        params.put("humidity", 60);
        params.put("pressure", 40000);
        params.put("mode", "auto");
        params.put("temperature", 25.6);
        // mock 方法
        mockConfig(inputPoint, pressurePoint, modePoint, point);
        mockConnection();
        clientUtilsMock.when(() -> IotModbusTcpClientUtils.write(same(connection), eq(SLAVE_ID), same(point),
                aryEq(new int[]{256}), any())).thenReturn(Future.succeededFuture(true));

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(randomPropertySetMessage(params)));

        // 断言：单个属性失败不影响其它属性；结果按参数顺序
        assertEquals(Arrays.asList("unknown", "humidity", "pressure", "mode", "temperature"),
                new ArrayList<>(results.keySet()));
        assertResult(results.get("unknown"), PROPERTY_SET_POINT_NOT_EXISTS);
        assertResult(results.get("humidity"), PROPERTY_SET_POINT_NOT_WRITABLE);
        assertResult(results.get("pressure"), PROPERTY_SET_VALUE_INVALID);
        assertResult(results.get("mode"), PROPERTY_SET_VALUE_INVALID);
        assertTrue(results.get("temperature").succeeded());
        clientUtilsMock.verify(() -> IotModbusTcpClientUtils.write(any(), any(), any(), any(), any()), times(1));
    }

    @Test
    public void testHandle_writeCoilAndBit() {
        // 准备参数：线圈 switch（布尔 true => 1），寄存器位点位 alarm（读-改-写）
        IotModbusPointRespDTO coilPoint = randomPoint(o -> o.setIdentifier("switch").setFunctionCode(FC_READ_COILS)
                .setRawDataType(IotModbusRawDataTypeEnum.BOOLEAN.getType()));
        IotModbusPointRespDTO bitPoint = randomPoint(o -> o.setIdentifier("alarm")
                .setRawDataType(IotModbusRawDataTypeEnum.BOOLEAN.getType()).setBitIndex(3));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("switch", true);
        params.put("alarm", "false");
        // mock 方法
        mockConfig(coilPoint, bitPoint);
        mockConnection();
        clientUtilsMock.when(() -> IotModbusTcpClientUtils.write(same(connection), eq(SLAVE_ID), same(coilPoint),
                aryEq(new int[]{1}), any())).thenReturn(Future.succeededFuture(true));
        clientUtilsMock.when(() -> IotModbusTcpClientUtils.writeBit(same(connection), eq(SLAVE_ID), same(bitPoint),
                eq(false), any())).thenReturn(Future.succeededFuture(true));

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(randomPropertySetMessage(params)));

        // 断言
        assertTrue(results.get("switch").succeeded());
        assertTrue(results.get("alarm").succeeded());
    }

    @Test
    public void testHandle_writeFailure() {
        // 准备参数：speed 设备异常响应（02 非法数据地址）；power 超时；mode 提交被拒绝（连接已关闭）
        IotModbusPointRespDTO speedPoint = randomPoint(o -> o.setIdentifier("speed"));
        IotModbusPointRespDTO powerPoint = randomPoint(o -> o.setIdentifier("power"));
        IotModbusPointRespDTO modePoint = randomPoint(o -> o.setIdentifier("mode"));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("speed", 1);
        params.put("power", 2);
        params.put("mode", 3);
        // mock 方法
        mockConfig(speedPoint, powerPoint, modePoint);
        mockConnection();
        clientUtilsMock.when(() -> IotModbusTcpClientUtils.write(any(), any(), same(speedPoint), any(), any()))
                .thenReturn(Future.failedFuture(new RuntimeException(new ModbusSlaveException(2))));
        clientUtilsMock.when(() -> IotModbusTcpClientUtils.write(any(), any(), same(powerPoint), any(), any()))
                .thenReturn(Future.failedFuture(new SocketTimeoutException("Read timed out")));
        clientUtilsMock.when(() -> IotModbusTcpClientUtils.write(any(), any(), same(modePoint), any(), any()))
                .thenThrow(new RejectedExecutionException("context closed"));

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(randomPropertySetMessage(params)));

        // 断言：异常码带回；超时 110；其它 106
        assertResult(results.get("speed"), MODBUS_EXCEPTION);
        assertEquals("2", results.get("speed").getStatusCode());
        assertResult(results.get("power"), MODBUS_TIMEOUT);
        assertResult(results.get("mode"), DEVICE_OFFLINE_OR_WRITE_FAILED);
        clientUtilsMock.verify(() -> IotModbusTcpClientUtils.writeBit(any(), any(), any(), anyBoolean(), any()), never());
    }

    @Test
    public void testHandle_pointChangedBeforeExecute() {
        // 准备参数：speed 在连接的有序队列里等待期间地址变化；power 被禁用（不在配置中）；mode 未变化
        IotModbusPointRespDTO speedPoint = randomPoint(o -> o.setIdentifier("speed").setRegisterAddress(0x10));
        IotModbusPointRespDTO powerPoint = randomPoint(o -> o.setIdentifier("power"));
        IotModbusPointRespDTO modePoint = randomPoint(o -> o.setIdentifier("mode"));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("speed", 1);
        params.put("power", 2);
        params.put("mode", 3);
        // mock 方法：下发时的配置；轮到写入时的配置
        IotModbusDeviceConfigRespDTO config = randomPojo(IotModbusDeviceConfigRespDTO.class,
                o -> o.setDeviceId(DEVICE_ID).setPoints(Arrays.asList(speedPoint, powerPoint, modePoint)));
        IotModbusPointRespDTO changedSpeedPoint = randomPoint(o -> o.setIdentifier("speed").setRegisterAddress(0x20));
        changedSpeedPoint.setId(speedPoint.getId());
        IotModbusDeviceConfigRespDTO changedConfig = randomPojo(IotModbusDeviceConfigRespDTO.class,
                o -> o.setDeviceId(DEVICE_ID).setPoints(Arrays.asList(changedSpeedPoint, modePoint)));
        when(configCacheService.getConfig(DEVICE_ID)).thenReturn(config, changedConfig);
        mockConnection();
        when(connectionManager.isCurrentTarget(same(changedConfig), same(connection), eq(SLAVE_ID))).thenReturn(true);
        mockWriteRunCheck();

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(randomPropertySetMessage(params)));

        // 断言：变化、禁用的点位不按旧配置写入；未变化的正常写入
        assertResult(results.get("speed"), PROPERTY_SET_POINT_CHANGED);
        assertResult(results.get("power"), PROPERTY_SET_POINT_CHANGED);
        assertTrue(results.get("mode").succeeded());
    }

    @Test
    public void testHandle_connectionChangedBeforeExecute() {
        // 准备参数
        IotModbusPointRespDTO point = randomPoint(o -> o.setIdentifier("speed"));
        // mock 方法：下发时的配置；轮到写入时，设备已迁移到其它 IP / 端口，或从站号已变化
        IotModbusDeviceConfigRespDTO config = randomPojo(IotModbusDeviceConfigRespDTO.class,
                o -> o.setDeviceId(DEVICE_ID).setPoints(Collections.singletonList(point)));
        IotModbusDeviceConfigRespDTO changedConfig = randomPojo(IotModbusDeviceConfigRespDTO.class,
                o -> o.setDeviceId(DEVICE_ID).setPoints(Collections.singletonList(point)));
        when(configCacheService.getConfig(DEVICE_ID)).thenReturn(config, changedConfig);
        mockConnection();
        when(connectionManager.isCurrentTarget(same(changedConfig), same(connection), eq(SLAVE_ID))).thenReturn(false);
        mockWriteRunCheck();

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(
                randomPropertySetMessage(singletonMap("speed", 1))));

        // 断言：不写旧连接、旧从站号
        assertResult(results.get("speed"), PROPERTY_SET_CONNECTION_CHANGED);
    }

    /**
     * mock 写入：与 executeBlocking 一致，先执行发出前的核对，抛出时写入失败
     */
    private void mockWriteRunCheck() {
        clientUtilsMock.when(() -> IotModbusTcpClientUtils.write(any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    try {
                        ((Runnable) invocation.getArgument(4)).run();
                    } catch (RuntimeException e) {
                        return Future.failedFuture(e);
                    }
                    return Future.succeededFuture(true);
                });
    }

    private void mockConfig(IotModbusPointRespDTO... points) {
        IotModbusDeviceConfigRespDTO config = randomPojo(IotModbusDeviceConfigRespDTO.class,
                o -> o.setDeviceId(DEVICE_ID).setPoints(Arrays.asList(points)));
        when(configCacheService.getConfig(DEVICE_ID)).thenReturn(config);
    }

    private void mockConnection() {
        when(connectionManager.getConnection(DEVICE_ID)).thenReturn(connection);
        when(connectionManager.getSlaveId(DEVICE_ID)).thenReturn(SLAVE_ID);
    }

    private static Map<String, IotDevicePropertySetResultDTO> await(Future<Map<String, IotDevicePropertySetResultDTO>> future) {
        try {
            return future.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static void assertResult(IotDevicePropertySetResultDTO result, ErrorCode errorCode) {
        assertNotNull(result);
        assertFalse(result.succeeded());
        assertEquals(errorCode.getCode(), result.getCode());
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static IotModbusPointRespDTO randomPoint(Consumer<IotModbusPointRespDTO>... consumers) {
        Consumer<IotModbusPointRespDTO> consumer = (o) -> {
            o.setIdentifier(randomString());
            o.setFunctionCode(FC_READ_HOLDING_REGISTERS); // 默认：保持寄存器 INT16 普通点位
            o.setRegisterAddress(RandomUtil.randomInt(0, 0xFFFF));
            o.setRegisterCount(1);
            o.setBitIndex(null);
            o.setByteOrder(IotModbusByteOrderEnum.AB.getOrder());
            o.setRawDataType(IotModbusRawDataTypeEnum.INT16.getType());
            o.setScale(BigDecimal.ONE);
        };
        return randomPojo(IotModbusPointRespDTO.class, ArrayUtils.append(consumer, consumers));
    }

    private static IotDeviceMessage randomPropertySetMessage(Object params) {
        return randomMessage(IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod(), params);
    }

    private static IotDeviceMessage randomMessage(String method, Object params) {
        return new IotDeviceMessage().setId(randomString()).setDeviceId(DEVICE_ID)
                .setMethod(method).setParams(params);
    }

}
