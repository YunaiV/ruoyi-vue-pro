package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.handler.downstream;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusByteOrderEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusRawDataTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertySetResultDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager.IotModbusTx;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager.ConnectionInfo;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerTransactionChannel;
import io.vertx.core.Future;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils.*;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link IotModbusTcpServerDownstreamHandler} 的单元测试
 *
 * @author 芋道源码
 */
public class IotModbusTcpServerDownstreamHandlerTest extends BaseMockitoUnitTest {

    private static final Long DEVICE_ID = 1L;
    private static final int SLAVE_ID = 1;

    @InjectMocks
    private IotModbusTcpServerDownstreamHandler downstreamHandler;

    @Mock
    private IotModbusTcpServerConnectionManager connectionManager;
    @Mock
    private IotModbusTcpServerConfigCacheService configCacheService;
    @Mock
    private IotModbusTcpServerTransactionChannel channel;

    @Test
    public void testHandle_propertyPost() {
        // 准备参数
        IotDeviceMessage message = randomMessage(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                singletonMap("temperature", 25));

        // 调用，并断言：上行消息直接忽略，不回复
        assertNull(downstreamHandler.handle(message));
        verifyNoInteractions(configCacheService, connectionManager, channel);
    }

    @Test
    public void testHandle_notPropertySet() {
        // 调用，并断言：服务调用、属性设置的回复都直接忽略，不回复
        assertNull(downstreamHandler.handle(randomMessage(IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod(),
                singletonMap("temperature", 25))));
        assertNull(downstreamHandler.handle(randomPropertySetMessage(singletonMap("temperature", 25)).setCode(0)));
        verifyNoInteractions(configCacheService, connectionManager, channel);
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
        verifyNoInteractions(configCacheService, connectionManager, channel);
    }

    @Test
    public void testHandle_configNotExists() {
        // 准备参数
        IotDeviceMessage message = randomPropertySetMessage(singletonMap("temperature", 25));
        // mock 方法：无配置
        when(configCacheService.getConfig(DEVICE_ID)).thenReturn(null);

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(message));
        // 断言：每个属性都失败；不再查连接，也不提交事务
        assertResult(results.get("temperature"), PROPERTY_SET_DEVICE_NOT_CONFIGURED);
        verifyNoInteractions(connectionManager, channel);
    }

    @Test
    public void testHandle_connectionNotExists() {
        // 准备参数
        IotModbusPointRespDTO point = randomPoint();
        IotDeviceMessage message = randomPropertySetMessage(singletonMap(point.getIdentifier(), 25));
        // mock 方法：有配置、无连接
        mockConfig(point);
        when(connectionManager.getConnectionInfoByDeviceId(DEVICE_ID)).thenReturn(null);

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(message));
        // 断言：设备离线
        assertResult(results.get(point.getIdentifier()), DEVICE_OFFLINE_OR_WRITE_FAILED);
        verifyNoInteractions(channel);
    }

    @Test
    public void testHandle_pointNotWritable() {
        // 准备参数：unknown 没有点位配置；humidity 是只读的输入寄存器（FC04）；door 是只读的离散输入（FC02）
        IotModbusPointRespDTO inputPoint = randomPoint(o -> o.setIdentifier("humidity")
                .setFunctionCode(FC_READ_INPUT_REGISTERS));
        IotModbusPointRespDTO discretePoint = randomPoint(o -> o.setIdentifier("door")
                .setFunctionCode(FC_READ_DISCRETE_INPUTS).setRawDataType(IotModbusRawDataTypeEnum.BOOLEAN.getType()));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("unknown", 1);
        params.put("humidity", 60);
        params.put("door", 1);
        IotDeviceMessage message = randomPropertySetMessage(params);
        // mock 方法
        mockConfig(inputPoint, discretePoint);
        mockConnection(SLAVE_ID, IotModbusFrameFormatEnum.MODBUS_TCP);

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(message));
        // 断言：均失败，且不提交事务；结果按参数顺序
        assertEquals(Arrays.asList("unknown", "humidity", "door"), new ArrayList<>(results.keySet()));
        assertResult(results.get("unknown"), PROPERTY_SET_POINT_NOT_EXISTS);
        assertResult(results.get("humidity"), PROPERTY_SET_POINT_NOT_WRITABLE);
        assertResult(results.get("door"), PROPERTY_SET_POINT_NOT_WRITABLE);
        verify(channel, never()).submitRaw(any());
    }

    @Test
    public void testHandle_partialFailure() {
        // 准备参数：位点位 alarm（保持寄存器 0x0010 的第 10 位）、超出 INT16 范围的 pressure、普通点位 temperature（保持寄存器 0x0011）
        IotModbusPointRespDTO bitPoint = randomPoint(o -> o.setIdentifier("alarm").setRegisterAddress(0x10)
                .setRawDataType(IotModbusRawDataTypeEnum.BOOLEAN.getType()).setBitIndex(10));
        IotModbusPointRespDTO pressurePoint = randomPoint(o -> o.setIdentifier("pressure").setRegisterAddress(0x12));
        IotModbusPointRespDTO point = randomPoint(o -> o.setIdentifier("temperature").setRegisterAddress(0x11));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("alarm", true);
        params.put("pressure", 40000);
        params.put("temperature", 25);
        IotDeviceMessage message = randomPropertySetMessage(params);
        // mock 方法：配置、连接；设备回显写请求
        mockConfig(bitPoint, pressurePoint, point);
        mockConnection(SLAVE_ID, IotModbusFrameFormatEnum.MODBUS_TCP);
        List<byte[]> pdus = mockSubmit(tx -> tx.complete(tx.getPdu()));

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(message));

        // 断言：位点位不写出（需要读-改-写，整字写会冲掉其它位）；越界值不截断；单个失败不影响普通点位写入
        assertResult(results.get("alarm"), MODBUS_BIT_WRITE_NOT_SUPPORTED);
        assertResult(results.get("pressure"), PROPERTY_SET_VALUE_INVALID);
        assertTrue(results.get("temperature").succeeded());
        assertEquals(1, pdus.size());
        assertArrayEquals(new byte[]{FC_WRITE_SINGLE_REGISTER, 0x00, 0x11, 0x00, 25}, pdus.get(0));
    }

    @Test
    public void testHandle_writeSingleRegister() {
        // 准备参数：保持寄存器 INT16，缩放 0.1：25.6 => 原始值 256
        IotModbusPointRespDTO point = randomPoint(o -> o.setScale(new BigDecimal("0.1")).setRegisterAddress(0x20));
        IotDeviceMessage message = randomPropertySetMessage(singletonMap(point.getIdentifier(), 25.6));
        // mock 方法
        mockConfig(point);
        mockConnection(SLAVE_ID, IotModbusFrameFormatEnum.MODBUS_TCP);
        List<byte[]> pdus = mockSubmit(tx -> tx.complete(tx.getPdu()));

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(message));
        // 断言
        assertTrue(results.get(point.getIdentifier()).succeeded());
        assertArrayEquals(new byte[]{FC_WRITE_SINGLE_REGISTER, 0x00, 0x20, 0x01, 0x00}, pdus.get(0));
    }

    @Test
    public void testHandle_writeSingleCoil() {
        // 准备参数：线圈（FC01）=> 单写线圈（FC05）；布尔值 true => 0xFF00
        IotModbusPointRespDTO point = randomPoint(o -> o.setFunctionCode(FC_READ_COILS).setRegisterAddress(0x05)
                .setRawDataType(IotModbusRawDataTypeEnum.BOOLEAN.getType()));
        IotDeviceMessage message = randomPropertySetMessage(singletonMap(point.getIdentifier(), true));
        // mock 方法
        mockConfig(point);
        mockConnection(SLAVE_ID, IotModbusFrameFormatEnum.MODBUS_TCP);
        List<byte[]> pdus = mockSubmit(tx -> tx.complete(tx.getPdu()));

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(message));
        // 断言
        assertTrue(results.get(point.getIdentifier()).succeeded());
        assertArrayEquals(new byte[]{FC_WRITE_SINGLE_COIL, 0x00, 0x05, (byte) 0xFF, 0x00}, pdus.get(0));
    }

    @Test
    public void testHandle_writeMultipleRegisters() {
        // 准备参数：保持寄存器 INT32（2 个寄存器，ABCD）=> 多写寄存器（FC16）：65537 => [0x0001, 0x0001]
        IotModbusPointRespDTO point = randomPoint(o -> o.setRegisterCount(2).setRegisterAddress(0x30)
                .setRawDataType(IotModbusRawDataTypeEnum.INT32.getType())
                .setByteOrder(IotModbusByteOrderEnum.ABCD.getOrder()));
        IotDeviceMessage message = randomPropertySetMessage(singletonMap(point.getIdentifier(), 65537));
        // mock 方法
        mockConfig(point);
        mockConnection(SLAVE_ID, IotModbusFrameFormatEnum.MODBUS_TCP);
        List<byte[]> pdus = mockSubmit(tx -> tx.complete(new byte[]{FC_WRITE_MULTIPLE_REGISTERS, 0x00, 0x30, 0x00, 0x02}));

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(message));
        // 断言
        assertTrue(results.get(point.getIdentifier()).succeeded());
        assertArrayEquals(new byte[]{FC_WRITE_MULTIPLE_REGISTERS, 0x00, 0x30, 0x00, 0x02, 0x04, 0x00, 0x01, 0x00, 0x01},
                pdus.get(0));
    }

    @Test
    public void testHandle_submitThrows() {
        // 准备参数：speed 提交时同步抛出异常（例如 owner 已关闭）；power 正常
        IotModbusPointRespDTO point1 = randomPoint(o -> o.setIdentifier("speed").setRegisterAddress(0x50));
        IotModbusPointRespDTO point2 = randomPoint(o -> o.setIdentifier("power").setRegisterAddress(0x51));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("speed", 1);
        params.put("power", 2);
        // mock 方法
        mockConfig(point1, point2);
        mockConnection(SLAVE_ID, IotModbusFrameFormatEnum.MODBUS_TCP);
        List<byte[]> pdus = mockSubmit(tx -> {
            if ((tx.getPdu()[2] & 0xFF) == 0x50) {
                throw new IllegalStateException("owner closed");
            }
            tx.complete(tx.getPdu());
        });

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(randomPropertySetMessage(params)));
        // 断言：只有 speed 失败，power 仍然写入并回复
        assertEquals(Arrays.asList("speed", "power"), new ArrayList<>(results.keySet()));
        assertResult(results.get("speed"), DEVICE_OFFLINE_OR_WRITE_FAILED);
        assertTrue(results.get("power").succeeded());
        assertEquals(2, pdus.size());
    }

    @Test
    public void testHandle_responseInvalid() {
        // 准备参数：speed 的异常响应缺少异常码（长度 1）；power 的回显与请求不一致
        IotModbusPointRespDTO point1 = randomPoint(o -> o.setIdentifier("speed").setRegisterAddress(0x60));
        IotModbusPointRespDTO point2 = randomPoint(o -> o.setIdentifier("power").setRegisterAddress(0x61));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("speed", 1);
        params.put("power", 2);
        // mock 方法
        mockConfig(point1, point2);
        mockConnection(SLAVE_ID, IotModbusFrameFormatEnum.MODBUS_TCP);
        mockSubmit(tx -> {
            if ((tx.getPdu()[2] & 0xFF) == 0x60) {
                tx.complete(new byte[]{(byte) (FC_WRITE_SINGLE_REGISTER | 0x80)});
            } else {
                tx.complete(new byte[]{FC_WRITE_SINGLE_REGISTER, 0x00, 0x61, 0x00, 0x03});
            }
        });

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(randomPropertySetMessage(params)));
        // 断言：都不能当作成功
        assertResult(results.get("speed"), MODBUS_RESPONSE_INVALID);
        assertResult(results.get("power"), MODBUS_RESPONSE_INVALID);
    }

    @Test
    public void testHandle_pointChangedBeforeSend() {
        // 准备参数：speed 排队期间地址变化；power 排队期间被禁用（不在配置中）；mode 未变化
        IotModbusPointRespDTO point1 = randomPoint(o -> o.setIdentifier("speed").setRegisterAddress(0x70));
        IotModbusPointRespDTO point2 = randomPoint(o -> o.setIdentifier("power").setRegisterAddress(0x71));
        IotModbusPointRespDTO point3 = randomPoint(o -> o.setIdentifier("mode").setRegisterAddress(0x72));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("speed", 1);
        params.put("power", 2);
        params.put("mode", 3);
        // mock 方法：发送前的配置
        mockConfig(point1, point2, point3);
        IotModbusPointRespDTO changedPoint1 = randomPoint(o -> o.setIdentifier("speed").setRegisterAddress(0x80));
        changedPoint1.setId(point1.getId());
        IotModbusPointRespDTO samePoint3 = randomPoint(o -> o.setIdentifier("mode").setRegisterAddress(0x72));
        samePoint3.setId(point3.getId());
        samePoint3.setName(point3.getName() + "-renamed"); // 与写入无关的字段变化不影响
        when(configCacheService.getCachedConfig(DEVICE_ID)).thenReturn(randomPojo(IotModbusDeviceConfigRespDTO.class,
                o -> o.setDeviceId(DEVICE_ID).setPoints(Arrays.asList(changedPoint1, samePoint3))));
        mockConnection(SLAVE_ID, IotModbusFrameFormatEnum.MODBUS_TCP);
        List<byte[]> pdus = mockSubmit(tx -> tx.complete(tx.getPdu()));

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(randomPropertySetMessage(params)));
        // 断言：变化的点位不写出旧地址，未变化的正常写入
        assertResult(results.get("speed"), PROPERTY_SET_POINT_CHANGED);
        assertResult(results.get("power"), PROPERTY_SET_POINT_CHANGED);
        assertTrue(results.get("mode").succeeded());
        assertEquals(1, pdus.size());
    }

    @Test
    public void testHandle_exceptionResponseAndFailure() {
        // 准备参数：两个可写点位
        IotModbusPointRespDTO point1 = randomPoint(o -> o.setIdentifier("speed").setRegisterAddress(0x40));
        IotModbusPointRespDTO point2 = randomPoint(o -> o.setIdentifier("power").setRegisterAddress(0x41));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("speed", 1);
        params.put("power", 2);
        IotDeviceMessage message = randomPropertySetMessage(params);
        // mock 方法：speed 设备返回异常响应（非法数据地址 02）；power 超时
        mockConfig(point1, point2);
        mockConnection(SLAVE_ID, IotModbusFrameFormatEnum.MODBUS_RTU);
        mockSubmit(tx -> {
            if ((tx.getPdu()[2] & 0xFF) == 0x40) {
                tx.complete(new byte[]{(byte) (FC_WRITE_SINGLE_REGISTER | 0x80), 0x02});
            } else {
                tx.fail(MODBUS_TIMEOUT);
            }
        });

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = await(downstreamHandler.handle(message));
        // 断言：异常码带回；网关判定的失败转为对应错误码
        assertResult(results.get("speed"), MODBUS_EXCEPTION);
        assertEquals("2", results.get("speed").getStatusCode());
        assertEquals("Modbus 异常响应（异常码 2）", results.get("speed").getMsg());
        assertResult(results.get("power"), MODBUS_TIMEOUT);
    }

    private IotModbusDeviceConfigRespDTO mockConfig(IotModbusPointRespDTO... points) {
        IotModbusDeviceConfigRespDTO config = randomPojo(IotModbusDeviceConfigRespDTO.class,
                o -> o.setDeviceId(DEVICE_ID).setPoints(Arrays.asList(points)));
        when(configCacheService.getConfig(DEVICE_ID)).thenReturn(config);
        // 发送前核对使用的本地缓存：默认与下发时一致（没有提交事务的用例不会用到）
        lenient().when(configCacheService.getCachedConfig(DEVICE_ID)).thenReturn(config);
        return config;
    }

    private void mockConnection(Integer slaveId, IotModbusFrameFormatEnum frameFormat) {
        when(connectionManager.getConnectionInfoByDeviceId(DEVICE_ID)).thenReturn(new ConnectionInfo()
                .setDeviceId(DEVICE_ID).setSlaveId(slaveId).setFrameFormat(frameFormat).setChannel(channel));
    }

    /**
     * mock 事务通道：与通道的 pump 一致，先执行发送前的核对（不通过时直接失败、不写出），再记录写出的 PDU，并按 responder 结束事务
     */
    private List<byte[]> mockSubmit(Consumer<IotModbusTx> responder) {
        List<byte[]> pdus = new ArrayList<>();
        doAnswer(invocation -> {
            IotModbusTx tx = invocation.getArgument(0);
            ErrorCode beforeSendFailure = tx.checkBeforeSend();
            if (beforeSendFailure != null) {
                tx.fail(beforeSendFailure);
                return null;
            }
            pdus.add(tx.getPdu());
            responder.accept(tx);
            return null;
        }).when(channel).submitRaw(any());
        return pdus;
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
