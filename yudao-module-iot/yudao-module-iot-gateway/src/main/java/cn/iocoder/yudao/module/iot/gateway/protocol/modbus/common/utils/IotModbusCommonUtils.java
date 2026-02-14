package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusByteOrderEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusRawDataTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrame;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * IoT Modbus 协议工具类
 * <p>
 * 提供 Modbus 协议全链路能力：
 * <ul>
 *   <li>协议常量：功能码（FC01~FC16）、异常掩码等</li>
 *   <li>功能码判断：读/写/异常分类、可写判断、写功能码映射</li>
 *   <li>CRC-16/MODBUS 计算和校验</li>
 *   <li>数据转换：原始值 ↔ 物模型属性值（{@link #convertToPropertyValue} / {@link #convertToRawValues}）</li>
 *   <li>帧值提取：从 Modbus 帧提取寄存器/线圈值（{@link #extractValues}）</li>
 *   <li>点位查找（{@link #findPoint}）</li>
 * </ul>
 *
 * @author 芋道源码
 */
@UtilityClass
@Slf4j
public class IotModbusCommonUtils {

    /** FC01: 读线圈 */
    public static final int FC_READ_COILS = 1;
    /** FC02: 读离散输入 */
    public static final int FC_READ_DISCRETE_INPUTS = 2;
    /** FC03: 读保持寄存器 */
    public static final int FC_READ_HOLDING_REGISTERS = 3;
    /** FC04: 读输入寄存器 */
    public static final int FC_READ_INPUT_REGISTERS = 4;

    /** FC05: 写单个线圈 */
    public static final int FC_WRITE_SINGLE_COIL = 5;
    /** FC06: 写单个寄存器 */
    public static final int FC_WRITE_SINGLE_REGISTER = 6;
    /** FC15: 写多个线圈 */
    public static final int FC_WRITE_MULTIPLE_COILS = 15;
    /** FC16: 写多个寄存器 */
    public static final int FC_WRITE_MULTIPLE_REGISTERS = 16;

    /**
     * 异常响应掩码：响应帧的功能码最高位为 1 时，表示异常响应
     * 例如：请求 FC=0x03，异常响应 FC=0x83（0x03 | 0x80）
     */
    public static final int FC_EXCEPTION_MASK = 0x80;

    /**
     * 功能码掩码：用于从异常响应中提取原始功能码
     * 例如：异常 FC=0x83，原始 FC = 0x83 & 0x7F = 0x03
     */
    public static final int FC_MASK = 0x7F;

    // ==================== 功能码分类判断 ====================

    /**
     * 判断是否为读响应（FC01-04）
     */
    public static boolean isReadResponse(int functionCode) {
        return functionCode >= FC_READ_COILS && functionCode <= FC_READ_INPUT_REGISTERS;
    }

    /**
     * 判断是否为写响应（FC05/06/15/16）
     */
    public static boolean isWriteResponse(int functionCode) {
        return functionCode == FC_WRITE_SINGLE_COIL || functionCode == FC_WRITE_SINGLE_REGISTER
                || functionCode == FC_WRITE_MULTIPLE_COILS || functionCode == FC_WRITE_MULTIPLE_REGISTERS;
    }

    /**
     * 判断是否为异常响应
     */
    public static boolean isExceptionResponse(int functionCode) {
        return (functionCode & FC_EXCEPTION_MASK) != 0;
    }

    /**
     * 从异常响应中提取原始功能码
     */
    public static int extractOriginalFunctionCode(int exceptionFunctionCode) {
        return exceptionFunctionCode & FC_MASK;
    }

    /**
     * 判断读功能码是否支持写操作
     * <p>
     * FC01（读线圈）和 FC03（读保持寄存器）支持写操作；
     * FC02（读离散输入）和 FC04（读输入寄存器）为只读。
     *
     * @param readFunctionCode 读功能码（FC01-04）
     * @return 是否支持写操作
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isWritable(int readFunctionCode) {
        return readFunctionCode == FC_READ_COILS || readFunctionCode == FC_READ_HOLDING_REGISTERS;
    }

    /**
     * 获取单写功能码
     * <p>
     * FC01（读线圈）→ FC05（写单个线圈）；
     * FC03（读保持寄存器）→ FC06（写单个寄存器）；
     * 其他返回 null（不支持写）。
     *
     * @param readFunctionCode 读功能码
     * @return 单写功能码，不支持写时返回 null
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    public static Integer getWriteSingleFunctionCode(int readFunctionCode) {
        switch (readFunctionCode) {
            case FC_READ_COILS:
                return FC_WRITE_SINGLE_COIL;
            case FC_READ_HOLDING_REGISTERS:
                return FC_WRITE_SINGLE_REGISTER;
            default:
                return null;
        }
    }

    /**
     * 获取多写功能码
     * <p>
     * FC01（读线圈）→ FC15（写多个线圈）；
     * FC03（读保持寄存器）→ FC16（写多个寄存器）；
     * 其他返回 null（不支持写）。
     *
     * @param readFunctionCode 读功能码
     * @return 多写功能码，不支持写时返回 null
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    public static Integer getWriteMultipleFunctionCode(int readFunctionCode) {
        switch (readFunctionCode) {
            case FC_READ_COILS:
                return FC_WRITE_MULTIPLE_COILS;
            case FC_READ_HOLDING_REGISTERS:
                return FC_WRITE_MULTIPLE_REGISTERS;
            default:
                return null;
        }
    }

    // ==================== CRC16 工具 ====================

    /**
     * 计算 CRC-16/MODBUS
     *
     * @param data   数据
     * @param length 计算长度
     * @return CRC16 值
     */
    public static int calculateCrc16(byte[] data, int length) {
        int crc = 0xFFFF;
        for (int i = 0; i < length; i++) {
            crc ^= (data[i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc >>= 1;
                    crc ^= 0xA001;
                } else {
                    crc >>= 1;
                }
            }
        }
        return crc;
    }

    /**
     * 校验 CRC16
     *
     * @param data 包含 CRC 的完整数据
     * @return 校验是否通过
     */
    public static boolean verifyCrc16(byte[] data) {
        if (data.length < 3) {
            return false;
        }
        int computed = calculateCrc16(data, data.length - 2);
        int received = (data[data.length - 2] & 0xFF) | ((data[data.length - 1] & 0xFF) << 8);
        return computed == received;
    }

    // ==================== 数据转换 ====================

    /**
     * 将原始值转换为物模型属性值
     *
     * @param rawValues 原始值数组（寄存器值或线圈值）
     * @param point     点位配置
     * @return 转换后的属性值
     */
    public static Object convertToPropertyValue(int[] rawValues, IotModbusPointRespDTO point) {
        if (ArrayUtil.isEmpty(rawValues)) {
            return null;
        }
        String rawDataType = point.getRawDataType();
        String byteOrder = point.getByteOrder();
        BigDecimal scale = ObjectUtil.defaultIfNull(point.getScale(), BigDecimal.ONE);

        // 1. 根据原始数据类型解析原始数值
        Number rawNumber = parseRawValue(rawValues, rawDataType, byteOrder);
        if (rawNumber == null) {
            return null;
        }

        // 2. 应用缩放因子：实际值 = 原始值 × scale
        BigDecimal actualValue = new BigDecimal(rawNumber.toString()).multiply(scale);

        // 3. 根据数据类型返回合适的 Java 类型
        return formatValue(actualValue, rawDataType);
    }

    /**
     * 将物模型属性值转换为原始寄存器值
     *
     * @param propertyValue 属性值
     * @param point         点位配置
     * @return 原始值数组
     */
    public static int[] convertToRawValues(Object propertyValue, IotModbusPointRespDTO point) {
        if (propertyValue == null) {
            return new int[0];
        }
        String rawDataType = point.getRawDataType();
        String byteOrder = point.getByteOrder();
        BigDecimal scale = ObjectUtil.defaultIfNull(point.getScale(), BigDecimal.ONE);
        int registerCount = ObjectUtil.defaultIfNull(point.getRegisterCount(), 1);

        // 1. 转换为 BigDecimal
        BigDecimal actualValue = new BigDecimal(propertyValue.toString());

        // 2. 应用缩放因子：原始值 = 实际值 ÷ scale
        BigDecimal rawValue = actualValue.divide(scale, 0, RoundingMode.HALF_UP);

        // 3. 根据原始数据类型编码为寄存器值
        return encodeToRegisters(rawValue, rawDataType, byteOrder, registerCount);
    }

    @SuppressWarnings("EnhancedSwitchMigration")
    private static Number parseRawValue(int[] rawValues, String rawDataType, String byteOrder) {
        IotModbusRawDataTypeEnum dataTypeEnum = IotModbusRawDataTypeEnum.getByType(rawDataType);
        if (dataTypeEnum == null) {
            log.warn("[parseRawValue][不支持的数据类型: {}]", rawDataType);
            return rawValues[0];
        }
        switch (dataTypeEnum) {
            case BOOLEAN:
                return rawValues[0] != 0 ? 1 : 0;
            case INT16:
                return (short) rawValues[0];
            case UINT16:
                return rawValues[0] & 0xFFFF;
            case INT32:
                return parseInt32(rawValues, byteOrder);
            case UINT32:
                return parseUint32(rawValues, byteOrder);
            case FLOAT:
                return parseFloat(rawValues, byteOrder);
            case DOUBLE:
                return parseDouble(rawValues, byteOrder);
            default:
                log.warn("[parseRawValue][不支持的数据类型: {}]", rawDataType);
                return rawValues[0];
        }
    }

    private static int parseInt32(int[] rawValues, String byteOrder) {
        if (rawValues.length < 2) {
            return rawValues[0];
        }
        byte[] bytes = reorderBytes(registersToBytes(rawValues, 2), byteOrder);
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    private static long parseUint32(int[] rawValues, String byteOrder) {
        if (rawValues.length < 2) {
            return rawValues[0] & 0xFFFFFFFFL;
        }
        byte[] bytes = reorderBytes(registersToBytes(rawValues, 2), byteOrder);
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt() & 0xFFFFFFFFL;
    }

    private static float parseFloat(int[] rawValues, String byteOrder) {
        if (rawValues.length < 2) {
            return (float) rawValues[0];
        }
        byte[] bytes = reorderBytes(registersToBytes(rawValues, 2), byteOrder);
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
    }

    private static double parseDouble(int[] rawValues, String byteOrder) {
        if (rawValues.length < 4) {
            return rawValues[0];
        }
        byte[] bytes = reorderBytes(registersToBytes(rawValues, 4), byteOrder);
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getDouble();
    }

    private static byte[] registersToBytes(int[] registers, int count) {
        byte[] bytes = new byte[count * 2];
        for (int i = 0; i < Math.min(registers.length, count); i++) {
            bytes[i * 2] = (byte) ((registers[i] >> 8) & 0xFF);
            bytes[i * 2 + 1] = (byte) (registers[i] & 0xFF);
        }
        return bytes;
    }

    @SuppressWarnings("EnhancedSwitchMigration")
    private static byte[] reorderBytes(byte[] bytes, String byteOrder) {
        IotModbusByteOrderEnum byteOrderEnum = IotModbusByteOrderEnum.getByOrder(byteOrder);
        // null 或者大端序，不需要调整
        if (ObjectUtils.equalsAny(byteOrderEnum, null, IotModbusByteOrderEnum.ABCD, IotModbusByteOrderEnum.AB)) {
            return bytes;
        }

        // 其他字节序调整
        byte[] result = new byte[bytes.length];
        switch (byteOrderEnum) {
            case BA: // 小端序：按每 2 字节一组交换（16 位场景 [1,0]，32 位场景 [1,0,3,2]）
                for (int i = 0; i + 1 < bytes.length; i += 2) {
                    result[i] = bytes[i + 1];
                    result[i + 1] = bytes[i];
                }
                break;
            case CDAB: // 大端字交换（32 位）
                if (bytes.length >= 4) {
                    result[0] = bytes[2];
                    result[1] = bytes[3];
                    result[2] = bytes[0];
                    result[3] = bytes[1];
                }
                break;
            case DCBA: // 小端序（32 位）
                if (bytes.length >= 4) {
                    result[0] = bytes[3];
                    result[1] = bytes[2];
                    result[2] = bytes[1];
                    result[3] = bytes[0];
                }
                break;
            case BADC: // 小端字交换（32 位）
                if (bytes.length >= 4) {
                    result[0] = bytes[1];
                    result[1] = bytes[0];
                    result[2] = bytes[3];
                    result[3] = bytes[2];
                }
                break;
            default:
                return bytes;
        }
        return result;
    }

    @SuppressWarnings("EnhancedSwitchMigration")
    private static int[] encodeToRegisters(BigDecimal rawValue, String rawDataType, String byteOrder, int registerCount) {
        IotModbusRawDataTypeEnum dataTypeEnum = IotModbusRawDataTypeEnum.getByType(rawDataType);
        if (dataTypeEnum == null) {
            return new int[]{rawValue.intValue()};
        }
        switch (dataTypeEnum) {
            case BOOLEAN:
                return new int[]{rawValue.intValue() != 0 ? 1 : 0};
            case INT16:
            case UINT16:
                return new int[]{rawValue.intValue() & 0xFFFF};
            case INT32:
                return encodeInt32(rawValue.intValue(), byteOrder);
            case UINT32:
                // 使用 longValue() 避免超过 Integer.MAX_VALUE 时溢出，
                // 强转 int 保留低 32 位 bit pattern，写入寄存器的字节是正确的无符号值
                return encodeInt32((int) rawValue.longValue(), byteOrder);
            case FLOAT:
                return encodeFloat(rawValue.floatValue(), byteOrder);
            case DOUBLE:
                return encodeDouble(rawValue.doubleValue(), byteOrder);
            default:
                return new int[]{rawValue.intValue()};
        }
    }

    private static int[] encodeInt32(int value, String byteOrder) {
        byte[] bytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(value).array();
        bytes = reorderBytes(bytes, byteOrder);
        return bytesToRegisters(bytes);
    }

    private static int[] encodeFloat(float value, String byteOrder) {
        byte[] bytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putFloat(value).array();
        bytes = reorderBytes(bytes, byteOrder);
        return bytesToRegisters(bytes);
    }

    private static int[] encodeDouble(double value, String byteOrder) {
        byte[] bytes = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putDouble(value).array();
        bytes = reorderBytes(bytes, byteOrder);
        return bytesToRegisters(bytes);
    }

    private static int[] bytesToRegisters(byte[] bytes) {
        int[] registers = new int[bytes.length / 2];
        for (int i = 0; i < registers.length; i++) {
            registers[i] = ((bytes[i * 2] & 0xFF) << 8) | (bytes[i * 2 + 1] & 0xFF);
        }
        return registers;
    }

    @SuppressWarnings("EnhancedSwitchMigration")
    private static Object formatValue(BigDecimal value, String rawDataType) {
        IotModbusRawDataTypeEnum dataTypeEnum = IotModbusRawDataTypeEnum.getByType(rawDataType);
        if (dataTypeEnum == null) {
            return value;
        }
        switch (dataTypeEnum) {
            case BOOLEAN:
                return value.intValue() != 0;
            case INT16:
            case INT32:
                return value.intValue();
            case UINT16:
            case UINT32:
                return value.longValue();
            case FLOAT:
                return value.floatValue();
            case DOUBLE:
                return value.doubleValue();
            default:
                return value;
        }
    }

    // ==================== 帧值提取 ====================

    /**
     * 从帧中提取寄存器值（FC01-04 读响应）
     *
     * @param frame 解码后的 Modbus 帧
     * @return 寄存器值数组（int[]），失败返回 null
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    public static int[] extractValues(IotModbusFrame frame) {
        if (frame == null || frame.isException()) {
            return null;
        }
        byte[] pdu = frame.getPdu();
        if (pdu == null || pdu.length < 1) {
            return null;
        }

        int functionCode = frame.getFunctionCode();
        switch (functionCode) {
            case FC_READ_COILS:
            case FC_READ_DISCRETE_INPUTS:
                return extractCoilValues(pdu);
            case FC_READ_HOLDING_REGISTERS:
            case FC_READ_INPUT_REGISTERS:
                return extractRegisterValues(pdu);
            default:
                log.warn("[extractValues][不支持的功能码: {}]", functionCode);
                return null;
        }
    }

    private static int[] extractCoilValues(byte[] pdu) {
        if (pdu.length < 2) {
            return null;
        }
        int byteCount = pdu[0] & 0xFF;
        int bitCount = byteCount * 8;
        int[] values = new int[bitCount];
        for (int i = 0; i < bitCount && (1 + i / 8) < pdu.length; i++) {
            values[i] = ((pdu[1 + i / 8] >> (i % 8)) & 0x01);
        }
        return values;
    }

    private static int[] extractRegisterValues(byte[] pdu) {
        if (pdu.length < 2) {
            return null;
        }
        int byteCount = pdu[0] & 0xFF;
        int registerCount = byteCount / 2;
        int[] values = new int[registerCount];
        for (int i = 0; i < registerCount && (1 + i * 2 + 1) < pdu.length; i++) {
            values[i] = ((pdu[1 + i * 2] & 0xFF) << 8) | (pdu[1 + i * 2 + 1] & 0xFF);
        }
        return values;
    }

    /**
     * 从响应帧中提取 registerCount（通过 PDU 的 byteCount 推断）
     *
     * @param frame 解码后的 Modbus 响应帧
     * @return registerCount，无法提取时返回 -1（匹配时跳过校验）
     */
    public static int extractRegisterCountFromResponse(IotModbusFrame frame) {
        byte[] pdu = frame.getPdu();
        if (pdu == null || pdu.length < 1) {
            return -1;
        }
        int byteCount = pdu[0] & 0xFF;
        int fc = frame.getFunctionCode();
        // FC03/04 寄存器读响应：registerCount = byteCount / 2
        if (fc == FC_READ_HOLDING_REGISTERS || fc == FC_READ_INPUT_REGISTERS) {
            return byteCount / 2;
        }
        // FC01/02 线圈/离散输入读响应：按 bit 打包有余位，无法精确反推，返回 -1 跳过校验
        return -1;
    }

    // ==================== 点位查找 ====================

    /**
     * 查找点位配置
     *
     * @param config     设备 Modbus 配置
     * @param identifier 点位标识符
     * @return 匹配的点位配置，未找到返回 null
     */
    public static IotModbusPointRespDTO findPoint(IotModbusDeviceConfigRespDTO config, String identifier) {
        if (config == null || StrUtil.isBlank(identifier)) {
            return null;
        }
        return CollUtil.findOne(config.getPoints(), p -> identifier.equals(p.getIdentifier()));
    }

    /**
     * 根据点位 ID 查找点位配置
     *
     * @param config  设备 Modbus 配置
     * @param pointId 点位 ID
     * @return 匹配的点位配置，未找到返回 null
     */
    public static IotModbusPointRespDTO findPointById(IotModbusDeviceConfigRespDTO config, Long pointId) {
        if (config == null || pointId == null) {
            return null;
        }
        return CollUtil.findOne(config.getPoints(), p -> p.getId().equals(pointId));
    }

}
