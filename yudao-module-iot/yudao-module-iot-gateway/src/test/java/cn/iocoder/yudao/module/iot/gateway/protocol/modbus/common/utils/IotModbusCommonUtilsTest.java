package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusRawDataTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrame;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.MODBUS_EXCEPTION;
import static cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils.FC_READ_COILS;
import static cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils.FC_READ_HOLDING_REGISTERS;
import static cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils.FC_READ_INPUT_REGISTERS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotModbusCommonUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class IotModbusCommonUtilsTest {

    @Test
    public void testBuildResponsePdu_exception() {
        // 准备参数：解码器已把 0x83 还原为原功能码 3，异常码单独存放
        IotModbusFrame frame = new IotModbusFrame().setSlaveId(1).setFunctionCode(3)
                .setPdu(new byte[]{0x02}).setExceptionCode(2);

        // 调用 + 断言：还原 0x80 异常位
        assertArrayEquals(HexUtil.decodeHex("8302"), IotModbusCommonUtils.buildResponsePdu(frame));
    }

    @Test
    public void testBuildResponsePdu_normal() {
        // 准备参数：解码器的 pdu 是功能码之后的数据（含 byteCount）
        IotModbusFrame frame = new IotModbusFrame().setSlaveId(1).setFunctionCode(3)
                .setPdu(HexUtil.decodeHex("0400FA0258"));

        // 调用 + 断言
        assertArrayEquals(HexUtil.decodeHex("030400FA0258"), IotModbusCommonUtils.buildResponsePdu(frame));
    }

    @Test
    public void testIsResponseShapeValid() {
        // FC03 读 2 个寄存器：byteCount = 4
        assertTrue(IotModbusCommonUtils.isResponseShapeValid(HexUtil.decodeHex("0300100002"),
                HexUtil.decodeHex("030400FA0258")));
        assertFalse(IotModbusCommonUtils.isResponseShapeValid(HexUtil.decodeHex("0300100002"),
                HexUtil.decodeHex("030200FA")));
        // FC01 读 10 个线圈：byteCount = 2
        assertTrue(IotModbusCommonUtils.isResponseShapeValid(HexUtil.decodeHex("010000000A"),
                HexUtil.decodeHex("01020103")));
        // FC06：原样回显
        assertTrue(IotModbusCommonUtils.isResponseShapeValid(HexUtil.decodeHex("06001000DC"),
                HexUtil.decodeHex("06001000DC")));
        assertFalse(IotModbusCommonUtils.isResponseShapeValid(HexUtil.decodeHex("06001000DC"),
                HexUtil.decodeHex("06001000DD")));
        // FC16：回显地址和数量
        assertTrue(IotModbusCommonUtils.isResponseShapeValid(HexUtil.decodeHex("100010000204000A0102"),
                HexUtil.decodeHex("1000100002")));
        assertFalse(IotModbusCommonUtils.isResponseShapeValid(HexUtil.decodeHex("100010000204000A0102"),
                HexUtil.decodeHex("1000100001")));
    }

    @Test
    public void testGetRawResponseError() {
        // 异常响应 83 02：MODBUS_EXCEPTION，文案带异常码
        ErrorCode error = IotModbusCommonUtils.getRawResponseError(HexUtil.decodeHex("8302"));
        assertNotNull(error);
        assertEquals(MODBUS_EXCEPTION.getCode(), error.getCode());
        assertEquals(StrUtil.format(MODBUS_EXCEPTION.getMsg(), 2), error.getMsg());
        // 正常响应：没有设备错误
        assertNull(IotModbusCommonUtils.getRawResponseError(HexUtil.decodeHex("030400FA0258")));
    }

    // ========== 寄存器位 ==========

    @Test
    public void testConvertToPropertyValue_bitPoint() {
        for (BigDecimal scale : Arrays.asList(null, BigDecimal.ONE, new BigDecimal("0.1"), BigDecimal.ZERO)) {
            // 准备参数：0x8101 的第 0、8、15 位为 1
            IotModbusPointRespDTO point = randomBitPoint(0);
            point.setScale(scale);

            // 调用 + 断言：直接取目标位，不经过缩放因子（0.1、0 不会把 1 变成 false）
            assertEquals(Boolean.TRUE, IotModbusCommonUtils.convertToPropertyValue(new int[]{0x8101}, point));
            point.setBitIndex(8);
            assertEquals(Boolean.TRUE, IotModbusCommonUtils.convertToPropertyValue(new int[]{0x8101}, point));
            point.setBitIndex(15);
            assertEquals(Boolean.TRUE, IotModbusCommonUtils.convertToPropertyValue(new int[]{0x8101}, point));
            point.setBitIndex(9);
            assertEquals(Boolean.FALSE, IotModbusCommonUtils.convertToPropertyValue(new int[]{0x8101}, point));
            point.setBitIndex(0);
            assertEquals(Boolean.FALSE, IotModbusCommonUtils.convertToPropertyValue(new int[]{0x0000}, point));
        }
        // FC04 输入寄存器同样适用
        IotModbusPointRespDTO point = randomBitPoint(15);
        point.setFunctionCode(FC_READ_INPUT_REGISTERS);
        assertTrue(IotModbusCommonUtils.isBitPoint(point));
        assertEquals(Boolean.TRUE, IotModbusCommonUtils.convertToPropertyValue(new int[]{0x8101}, point));
    }

    @Test
    public void testConvertToPropertyValue_notBitPoint() {
        // 准备参数：0x8101 的第 1 位为 0；不满足位点位规则（例如历史脏数据）时，按原逻辑整个寄存器非 0 即 true
        IotModbusPointRespDTO coilPoint = randomBitPoint(1);
        coilPoint.setFunctionCode(FC_READ_COILS);
        IotModbusPointRespDTO twoRegistersPoint = randomBitPoint(1);
        twoRegistersPoint.setRegisterCount(2);
        IotModbusPointRespDTO nullRegisterCountPoint = randomBitPoint(1);
        nullRegisterCountPoint.setRegisterCount(null);
        IotModbusPointRespDTO tooLargeBitIndexPoint = randomBitPoint(16);
        IotModbusPointRespDTO negativeBitIndexPoint = randomBitPoint(-1);
        IotModbusPointRespDTO nullBitIndexPoint = randomBitPoint(null);

        // 调用 + 断言
        for (IotModbusPointRespDTO point : Arrays.asList(coilPoint, twoRegistersPoint, nullRegisterCountPoint,
                tooLargeBitIndexPoint, negativeBitIndexPoint, nullBitIndexPoint)) {
            assertFalse(IotModbusCommonUtils.isBitPoint(point));
            assertEquals(Boolean.TRUE, IotModbusCommonUtils.convertToPropertyValue(new int[]{0x8101}, point));
        }
        // 非 BOOLEAN：按原数据类型解析
        IotModbusPointRespDTO int16Point = randomBitPoint(1);
        int16Point.setRawDataType(IotModbusRawDataTypeEnum.INT16.getType());
        assertFalse(IotModbusCommonUtils.isBitPoint(int16Point));
        assertEquals((int) (short) 0x8101, IotModbusCommonUtils.convertToPropertyValue(new int[]{0x8101}, int16Point));
    }

    @Test
    public void testSetBit() {
        // 只修改目标位，其它位保持不变
        assertEquals(0x5E00, IotModbusCommonUtils.setBit(0x5A00, 10, true));
        assertEquals(0x5C00, IotModbusCommonUtils.setBit(0x5E00, 9, false));
        assertEquals(0x5A00, IotModbusCommonUtils.setBit(0x5A00, 9, true));
        // 第 15 位：结果保持在 16 位内
        assertEquals(0x8000, IotModbusCommonUtils.setBit(0x0000, 15, true));
        assertEquals(0x7FFF, IotModbusCommonUtils.setBit(0xFFFF, 15, false));
    }

    @Test
    public void testToBitValue() {
        // Boolean、"true" / "false"
        assertTrue(IotModbusCommonUtils.toBitValue(true));
        assertFalse(IotModbusCommonUtils.toBitValue(false));
        assertTrue(IotModbusCommonUtils.toBitValue("true"));
        assertFalse(IotModbusCommonUtils.toBitValue(" FALSE "));
        // 数字：0 为 false，非 0 为 true
        assertTrue(IotModbusCommonUtils.toBitValue(1));
        assertTrue(IotModbusCommonUtils.toBitValue(2));
        assertFalse(IotModbusCommonUtils.toBitValue(0));
        assertFalse(IotModbusCommonUtils.toBitValue("0.0"));
        // 其它：与原有写入转换一样抛出
        assertThrows(NumberFormatException.class, () -> IotModbusCommonUtils.toBitValue("on"));
    }

    @Test
    public void testConvertToRawValues_booleanUnchanged() {
        // 准备参数：未配置位索引的 BOOLEAN 点位
        IotModbusPointRespDTO point = randomBitPoint(null);

        // 调用 + 断言：保持原逻辑，先除以缩放因子再取整
        point.setScale(BigDecimal.ONE);
        assertArrayEquals(new int[]{1}, IotModbusCommonUtils.convertToRawValues(1, point));
        assertArrayEquals(new int[]{0}, IotModbusCommonUtils.convertToRawValues(0.1, point));
        point.setScale(BigDecimal.TEN);
        assertArrayEquals(new int[]{0}, IotModbusCommonUtils.convertToRawValues(1, point));
    }

    /**
     * 位点位：FC03、BOOLEAN、1 个寄存器
     */
    private static IotModbusPointRespDTO randomBitPoint(Integer bitIndex) {
        IotModbusPointRespDTO point = new IotModbusPointRespDTO();
        point.setFunctionCode(FC_READ_HOLDING_REGISTERS);
        point.setRegisterAddress(0x10);
        point.setRegisterCount(1);
        point.setRawDataType(IotModbusRawDataTypeEnum.BOOLEAN.getType());
        point.setBitIndex(bitIndex);
        return point;
    }

}
