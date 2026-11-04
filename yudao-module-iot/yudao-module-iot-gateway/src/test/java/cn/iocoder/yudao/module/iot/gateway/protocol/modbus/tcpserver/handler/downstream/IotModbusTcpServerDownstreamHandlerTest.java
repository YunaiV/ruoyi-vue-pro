package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.handler.downstream;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusByteOrderEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusRawDataTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrameEncoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager.ConnectionInfo;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerTransactionChannel;
import io.vertx.core.Future;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils.*;
import static java.util.Collections.singletonMap;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// DONE @AI：对齐 system user test；也补齐之前缺的单测噢；（已按 AdminUserServiceImplTest 的随机对象、“准备参数 / mock 方法 / 调用 / 断言”风格重写，补齐忽略消息、缺配置/连接、不可写点位、FC05/FC06/FC16 写入、默认从站等用例）
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
    private IotModbusFrameEncoder frameEncoder;
    @Mock
    private IotModbusTcpServerTransactionChannel channel;

    @Test
    public void testHandle_propertyPost() {
        // 准备参数
        IotDeviceMessage message = randomMessage(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                singletonMap("temperature", 25));

        // 调用
        downstreamHandler.handle(message);
        // 断言：上行消息直接忽略
        verifyNoInteractions(configCacheService, connectionManager, frameEncoder, channel);
    }

    @Test
    public void testHandle_notPropertySet() {
        // 准备参数
        IotDeviceMessage message = randomMessage(IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod(),
                singletonMap("temperature", 25));

        // 调用
        downstreamHandler.handle(message);
        // 断言：非属性设置消息直接忽略
        verifyNoInteractions(configCacheService, connectionManager, frameEncoder, channel);
    }

    @Test
    public void testHandle_configNotExists() {
        // 准备参数
        IotDeviceMessage message = randomPropertySetMessage(singletonMap("temperature", 25));
        // mock 方法：无配置
        when(configCacheService.getConfig(DEVICE_ID)).thenReturn(null);

        // 调用
        downstreamHandler.handle(message);
        // 断言：不再查连接，也不写出
        verifyNoInteractions(connectionManager, frameEncoder, channel);
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
        downstreamHandler.handle(message);
        // 断言：不写出
        verifyNoInteractions(frameEncoder, channel);
    }

    @Test
    public void testHandle_paramsNotMap() {
        // 准备参数
        IotModbusPointRespDTO point = randomPoint();
        IotDeviceMessage message = randomMessage(IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod(), randomString());
        // mock 方法
        mockConfig(point);
        mockConnection(SLAVE_ID, IotModbusFrameFormatEnum.MODBUS_TCP);

        // 调用
        downstreamHandler.handle(message);
        // 断言：不写出
        verifyNoInteractions(frameEncoder, channel);
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
        downstreamHandler.handle(message);
        // 断言：均跳过，不分配事务号，也不写出
        verifyNoInteractions(frameEncoder, channel);
    }

    @Test
    public void testHandle_bitPointNotWrite() {
        // 准备参数：位点位 alarm（保持寄存器 0x0010 的第 10 位）、普通点位 temperature（保持寄存器 0x0011）
        IotModbusPointRespDTO bitPoint = randomPoint(o -> o.setIdentifier("alarm").setRegisterAddress(0x10)
                .setRawDataType(IotModbusRawDataTypeEnum.BOOLEAN.getType()).setBitIndex(10));
        IotModbusPointRespDTO point = randomPoint(o -> o.setIdentifier("temperature").setRegisterAddress(0x11));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("alarm", true);
        params.put("temperature", 25);
        IotDeviceMessage message = randomPropertySetMessage(params);
        byte[] data = new byte[]{0x01};
        // mock 方法：配置、连接
        mockConfig(bitPoint, point);
        mockConnection(SLAVE_ID, IotModbusFrameFormatEnum.MODBUS_TCP);
        // mock 方法：普通点位的写入
        when(channel.allocateTransactionId()).thenReturn(1);
        when(frameEncoder.encodeWriteSingleRequest(SLAVE_ID, FC_WRITE_SINGLE_REGISTER, 0x11, 25,
                IotModbusFrameFormatEnum.MODBUS_TCP, 1)).thenReturn(data);
        when(channel.send(data)).thenReturn(Future.succeededFuture());

        // 调用
        downstreamHandler.handle(message);

        // 断言：位点位不写出（TCP Server 不等待设备响应，无法读-改-写，整字写会冲掉其它位）；普通点位照常写入
        verify(channel, times(1)).allocateTransactionId();
        verify(frameEncoder, times(1)).encodeWriteSingleRequest(anyInt(), anyInt(), anyInt(), anyInt(), any(), any());
        verify(channel, times(1)).send(data);
    }

    @Test
    public void testHandle_writeSingleRegister() {
        // 准备参数：保持寄存器 INT16，缩放 0.1：25.6 => 原始值 256
        IotModbusPointRespDTO point = randomPoint(o -> o.setScale(new BigDecimal("0.1")));
        IotDeviceMessage message = randomPropertySetMessage(singletonMap(point.getIdentifier(), 25.6));
        byte[] data = randomBytes();
        // mock 方法
        mockConfig(point);
        mockConnection(SLAVE_ID, IotModbusFrameFormatEnum.MODBUS_TCP);
        when(channel.allocateTransactionId()).thenReturn(1);
        when(frameEncoder.encodeWriteSingleRequest(SLAVE_ID, FC_WRITE_SINGLE_REGISTER, point.getRegisterAddress(), 256,
                IotModbusFrameFormatEnum.MODBUS_TCP, 1)).thenReturn(data);
        when(channel.send(data)).thenReturn(Future.succeededFuture());

        // 调用
        downstreamHandler.handle(message);
        // 断言
        verify(channel).send(data);
    }

    @Test
    public void testHandle_writeSingleCoil() {
        // 准备参数：线圈（FC01）=> 单写线圈（FC05）
        IotModbusPointRespDTO point = randomPoint(o -> o.setFunctionCode(FC_READ_COILS)
                .setRawDataType(IotModbusRawDataTypeEnum.BOOLEAN.getType()));
        IotDeviceMessage message = randomPropertySetMessage(singletonMap(point.getIdentifier(), 1));
        byte[] data = randomBytes();
        // mock 方法
        mockConfig(point);
        mockConnection(SLAVE_ID, IotModbusFrameFormatEnum.MODBUS_TCP);
        when(channel.allocateTransactionId()).thenReturn(2);
        when(frameEncoder.encodeWriteSingleRequest(SLAVE_ID, FC_WRITE_SINGLE_COIL, point.getRegisterAddress(), 1,
                IotModbusFrameFormatEnum.MODBUS_TCP, 2)).thenReturn(data);
        when(channel.send(data)).thenReturn(Future.succeededFuture());

        // 调用
        downstreamHandler.handle(message);
        // 断言
        verify(channel).send(data);
    }

    @Test
    public void testHandle_writeMultipleRegisters() {
        // 准备参数：保持寄存器 INT32（2 个寄存器，ABCD）=> 多写寄存器（FC16）：65537 => [0x0001, 0x0001]
        IotModbusPointRespDTO point = randomPoint(o -> o.setRegisterCount(2)
                .setRawDataType(IotModbusRawDataTypeEnum.INT32.getType())
                .setByteOrder(IotModbusByteOrderEnum.ABCD.getOrder()));
        IotDeviceMessage message = randomPropertySetMessage(singletonMap(point.getIdentifier(), 65537));
        byte[] data = randomBytes();
        // mock 方法
        mockConfig(point);
        mockConnection(SLAVE_ID, IotModbusFrameFormatEnum.MODBUS_TCP);
        when(channel.allocateTransactionId()).thenReturn(3);
        when(frameEncoder.encodeWriteMultipleRegistersRequest(eq(SLAVE_ID), eq(point.getRegisterAddress()),
                aryEq(new int[]{1, 1}), eq(IotModbusFrameFormatEnum.MODBUS_TCP), eq(3))).thenReturn(data);
        when(channel.send(data)).thenReturn(Future.succeededFuture());

        // 调用
        downstreamHandler.handle(message);
        // 断言
        verify(frameEncoder, never()).encodeWriteSingleRequest(anyInt(), anyInt(), anyInt(), anyInt(), any(), any());
        verify(channel).send(data);
    }

    @Test
    public void testHandle_defaultSlaveId() {
        // 准备参数
        IotModbusPointRespDTO point = randomPoint();
        IotDeviceMessage message = randomPropertySetMessage(singletonMap(point.getIdentifier(), 25));
        byte[] data = randomBytes();
        // mock 方法：连接未识别从站地址，帧格式为 RTU
        mockConfig(point);
        mockConnection(null, IotModbusFrameFormatEnum.MODBUS_RTU);
        when(channel.allocateTransactionId()).thenReturn(4);
        when(frameEncoder.encodeWriteSingleRequest(1, FC_WRITE_SINGLE_REGISTER, point.getRegisterAddress(), 25,
                IotModbusFrameFormatEnum.MODBUS_RTU, 4)).thenReturn(data);
        when(channel.send(data)).thenReturn(Future.failedFuture("socket closed"));

        // 调用
        downstreamHandler.handle(message);
        // 断言：从站地址默认 1，帧格式沿用连接；写出失败仅记录日志
        verify(channel).send(data);
    }

    private void mockConfig(IotModbusPointRespDTO... points) {
        IotModbusDeviceConfigRespDTO config = randomPojo(IotModbusDeviceConfigRespDTO.class,
                o -> o.setDeviceId(DEVICE_ID).setPoints(Arrays.asList(points)));
        when(configCacheService.getConfig(DEVICE_ID)).thenReturn(config);
    }

    private void mockConnection(Integer slaveId, IotModbusFrameFormatEnum frameFormat) {
        when(connectionManager.getConnectionInfoByDeviceId(DEVICE_ID)).thenReturn(new ConnectionInfo()
                .setDeviceId(DEVICE_ID).setSlaveId(slaveId).setFrameFormat(frameFormat).setChannel(channel));
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

    private static byte[] randomBytes() {
        return randomString().getBytes();
    }

}
