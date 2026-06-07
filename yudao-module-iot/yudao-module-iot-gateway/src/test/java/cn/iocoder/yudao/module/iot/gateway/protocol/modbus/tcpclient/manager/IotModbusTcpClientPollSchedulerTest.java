package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientPollScheduler.ReadSegment;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils.FC_READ_COILS;
import static cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils.FC_READ_HOLDING_REGISTERS;
import static cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils.FC_READ_INPUT_REGISTERS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotModbusTcpClientPollScheduler} 的单元测试
 *
 * @author 芋道源码
 */
public class IotModbusTcpClientPollSchedulerTest {

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
