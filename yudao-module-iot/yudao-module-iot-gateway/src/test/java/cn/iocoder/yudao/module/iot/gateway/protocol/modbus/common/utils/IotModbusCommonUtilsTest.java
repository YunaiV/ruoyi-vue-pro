package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusByteOrderEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusRawDataTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrame;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

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

    @Test
    public void testConvertToRawValues_booleanLiteral() {
        // 准备参数：线圈点位
        IotModbusPointRespDTO point = newPoint(FC_READ_COILS, IotModbusRawDataTypeEnum.BOOLEAN, 1);

        // 调用 + 断言：布尔字面量直接转为 1 / 0（不再走数值转换）
        assertArrayEquals(new int[]{1}, IotModbusCommonUtils.convertToRawValues(true, point));
        assertArrayEquals(new int[]{0}, IotModbusCommonUtils.convertToRawValues(false, point));
        assertArrayEquals(new int[]{1}, IotModbusCommonUtils.convertToRawValues("TRUE", point));
        assertArrayEquals(new int[]{0}, IotModbusCommonUtils.convertToRawValues(" false ", point));
        assertArrayEquals(new int[]{1}, IotModbusCommonUtils.convertToRawValues(1, point));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues("on", point));
    }

    @Test
    public void testConvertToRawValues_integerRange() {
        // INT16：[-32768, 32767]
        IotModbusPointRespDTO int16 = newPoint(FC_READ_HOLDING_REGISTERS, IotModbusRawDataTypeEnum.INT16, 1);
        assertArrayEquals(new int[]{0x7FFF}, IotModbusCommonUtils.convertToRawValues(32767, int16));
        assertArrayEquals(new int[]{0x8000}, IotModbusCommonUtils.convertToRawValues(-32768, int16));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(32768, int16));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(-32769, int16));
        // INT16 + 缩放：3276.8 ÷ 0.1 = 32768 超限，不截断
        int16.setScale(new BigDecimal("0.1"));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(3276.8, int16));
        // UINT16：[0, 65535]
        IotModbusPointRespDTO uint16 = newPoint(FC_READ_HOLDING_REGISTERS, IotModbusRawDataTypeEnum.UINT16, 1);
        assertArrayEquals(new int[]{0xFFFF}, IotModbusCommonUtils.convertToRawValues(65535, uint16));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(65536, uint16));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(-1, uint16));
        // INT32 / UINT32
        IotModbusPointRespDTO int32 = newPoint(FC_READ_HOLDING_REGISTERS, IotModbusRawDataTypeEnum.INT32, 2);
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(2147483648L, int32));
        IotModbusPointRespDTO uint32 = newPoint(FC_READ_HOLDING_REGISTERS, IotModbusRawDataTypeEnum.UINT32, 2);
        assertArrayEquals(new int[]{0xFFFF, 0xFFFF}, IotModbusCommonUtils.convertToRawValues(4294967295L, uint32));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(4294967296L, uint32));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(-1, uint32));
    }

    @Test
    public void testConvertToRawValues_floatKeepDecimal() {
        // 准备参数：FLOAT（ABCD）
        IotModbusPointRespDTO point = newPoint(FC_READ_HOLDING_REGISTERS, IotModbusRawDataTypeEnum.FLOAT, 2);

        // 调用 + 断言：保留小数，1.25 => 0x3FA00000（之前会被四舍五入为 1）
        assertArrayEquals(new int[]{0x3FA0, 0x0000}, IotModbusCommonUtils.convertToRawValues(1.25, point));
        // 缩放：0.25 ÷ 0.2 = 1.25
        point.setScale(new BigDecimal("0.2"));
        assertArrayEquals(new int[]{0x3FA0, 0x0000}, IotModbusCommonUtils.convertToRawValues("0.25", point));
    }

    @Test
    public void testConvertToRawValues_invalid() {
        IotModbusPointRespDTO point = newPoint(FC_READ_HOLDING_REGISTERS, IotModbusRawDataTypeEnum.INT16, 1);
        // 值：null、结构、数组、非数值
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(null, point));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(
                Collections.singletonMap("a", 1), point));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(
                Arrays.asList(1, 2), point));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(new int[]{1}, point));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues("abc", point));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(Double.NaN, point));
        // 缩放因子为 0
        point.setScale(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(1, point));
        // 寄存器数与数据类型不一致：INT32 只配置了 1 个寄存器
        IotModbusPointRespDTO int32 = newPoint(FC_READ_HOLDING_REGISTERS, IotModbusRawDataTypeEnum.INT32, 1);
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(1, int32));
    }

    @Test
    public void testConvertToRawValues_dataTypeUnknown() {
        // 准备参数：数据类型为空、未知
        IotModbusPointRespDTO point = newPoint(FC_READ_HOLDING_REGISTERS, IotModbusRawDataTypeEnum.INT16, 1);

        // 调用 + 断言：失败，不按整数猜测
        point.setRawDataType(null);
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(1, point));
        point.setRawDataType("UNKNOWN");
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(1, point));
    }

    @Test
    public void testConvertToRawValues_booleanLargeNumber() {
        // 准备参数：BOOLEAN 寄存器点位（未配置位索引）
        IotModbusPointRespDTO point = newPoint(FC_READ_HOLDING_REGISTERS, IotModbusRawDataTypeEnum.BOOLEAN, 1);

        // 调用 + 断言：非 0 即 1，不因 intValue 只取低 32 位而变成 0
        assertArrayEquals(new int[]{1}, IotModbusCommonUtils.convertToRawValues(4294967296L, point));
        assertArrayEquals(new int[]{1}, IotModbusCommonUtils.convertToRawValues(-2, point));
        assertArrayEquals(new int[]{1}, IotModbusCommonUtils.convertToRawValues("1e1000000000", point));
        assertArrayEquals(new int[]{0}, IotModbusCommonUtils.convertToRawValues("1e-1000000000", point));
    }

    @Test
    public void testConvertToRawValues_integerExtremeExponent() {
        // 准备参数：INT16
        IotModbusPointRespDTO point = newPoint(FC_READ_HOLDING_REGISTERS, IotModbusRawDataTypeEnum.INT16, 1);

        // 调用 + 断言：极大的科学计数法直接超限，极小的四舍五入为 0（都不分配巨大的 BigInteger）
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues("1e1000000000", point));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues("-1e1000000000", point));
        assertArrayEquals(new int[]{0}, IotModbusCommonUtils.convertToRawValues("1e-1000000000", point));
    }

    @Test
    public void testConvertToRawValues_floatBoundary() {
        // 准备参数：FLOAT（ABCD）
        IotModbusPointRespDTO point = newPoint(FC_READ_HOLDING_REGISTERS, IotModbusRawDataTypeEnum.FLOAT, 2);

        // 调用 + 断言：Float.MAX_VALUE 的 double 写法、十进制写法都是合法的最大值
        assertArrayEquals(new int[]{0x7F7F, 0xFFFF}, IotModbusCommonUtils.convertToRawValues((double) Float.MAX_VALUE, point));
        assertArrayEquals(new int[]{0x7F7F, 0xFFFF}, IotModbusCommonUtils.convertToRawValues("3.4028235E38", point));
        assertArrayEquals(new int[]{0xFF7F, 0xFFFF}, IotModbusCommonUtils.convertToRawValues(-Float.MAX_VALUE, point));
        assertArrayEquals(new int[]{0x0000, 0x0000}, IotModbusCommonUtils.convertToRawValues(0, point));
        // 上溢（Infinity）、下溢（非零值变成 0）失败
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues("3.5e38", point));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues(1e-50, point));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues("1e1000000000", point));
    }

    @Test
    public void testConvertToRawValues_doublePrecision() {
        // 准备参数：DOUBLE（4 个寄存器）
        IotModbusPointRespDTO point = newPoint(FC_READ_HOLDING_REGISTERS, IotModbusRawDataTypeEnum.DOUBLE, 4);

        // 调用 + 断言：scale = 1 时保留原值，不按 16 位有效数字舍入
        assertArrayEquals(IotModbusCommonUtils.convertToRawValues(1.0000000000000002, point),
                IotModbusCommonUtils.convertToRawValues(new BigDecimal("1.0000000000000002220446049250313"), point));
        assertFalse(Arrays.equals(IotModbusCommonUtils.convertToRawValues(1.0, point),
                IotModbusCommonUtils.convertToRawValues(1.0000000000000002, point)));
        // 调用 + 断言：缩放时按 DECIMAL128 相除，0.30000000000000004 ÷ 0.1 = 3.0000000000000004（不是 3.0）
        IotModbusPointRespDTO scaled = newPoint(FC_READ_HOLDING_REGISTERS, IotModbusRawDataTypeEnum.DOUBLE, 4);
        scaled.setScale(new BigDecimal("0.1"));
        assertArrayEquals(IotModbusCommonUtils.convertToRawValues(3.0000000000000004, point),
                IotModbusCommonUtils.convertToRawValues("0.30000000000000004", scaled));
        // 边界：Double.MAX_VALUE 合法；上溢、下溢失败
        IotModbusCommonUtils.convertToRawValues(Double.MAX_VALUE, point);
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues("1e309", point));
        assertThrows(IllegalArgumentException.class, () -> IotModbusCommonUtils.convertToRawValues("1e-400", point));
    }

    @Test
    public void testIsSameWriteTarget() {
        // 准备参数
        IotModbusPointRespDTO point = newPoint(FC_READ_HOLDING_REGISTERS, IotModbusRawDataTypeEnum.INT16, 1);
        point.setId(1L);
        point.setIdentifier("speed");
        point.setName("速度");
        IotModbusPointRespDTO same = newPoint(FC_READ_HOLDING_REGISTERS, IotModbusRawDataTypeEnum.INT16, 1);
        same.setId(1L);
        same.setIdentifier("speed");
        same.setName("新名称");
        same.setPollInterval(5000);
        same.setScale(new BigDecimal("1.00"));

        // 调用 + 断言：名称、轮询间隔、缩放因子的写法不同不影响
        assertTrue(IotModbusCommonUtils.isSameWriteTarget(point, same));
        // 调用 + 断言：点位已禁用 / 删除、地址或类型变化
        assertFalse(IotModbusCommonUtils.isSameWriteTarget(point, null));
        same.setRegisterAddress(0x11);
        assertFalse(IotModbusCommonUtils.isSameWriteTarget(point, same));
        same.setRegisterAddress(point.getRegisterAddress());
        same.setRawDataType(IotModbusRawDataTypeEnum.UINT16.getType());
        assertFalse(IotModbusCommonUtils.isSameWriteTarget(point, same));
    }

    private static IotModbusPointRespDTO newPoint(int functionCode, IotModbusRawDataTypeEnum rawDataType,
                                                  int registerCount) {
        IotModbusPointRespDTO point = new IotModbusPointRespDTO();
        point.setFunctionCode(functionCode);
        point.setRegisterAddress(0x10);
        point.setRegisterCount(registerCount);
        point.setRawDataType(rawDataType.getType());
        point.setByteOrder(registerCount == 1 ? IotModbusByteOrderEnum.AB.getOrder() : IotModbusByteOrderEnum.ABCD.getOrder());
        point.setScale(BigDecimal.ONE);
        return point;
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
