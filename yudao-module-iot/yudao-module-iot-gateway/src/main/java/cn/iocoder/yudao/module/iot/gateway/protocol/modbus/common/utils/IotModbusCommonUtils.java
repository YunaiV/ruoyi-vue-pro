package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusByteOrderEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusRawDataTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrame;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

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
        // 位点位：直接返回目标位，不经过缩放因子（否则 1 × 0.1 再取整会变成 false）
        if (isBitPoint(point)) {
            return getBit(rawValues[0], point.getBitIndex());
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
     * 将物模型属性值转换为原始寄存器值（写入）
     * <ul>
     *     <li>数据类型为空、未知或 STRING：失败（不按整数猜测）；</li>
     *     <li>BOOLEAN：true / false（含字符串）直接写 1 / 0；数值按原逻辑先除以缩放因子再取整，非 0 为 1；</li>
     *     <li>整数类型：原始值 = 实际值 ÷ scale（四舍五入取整，与原逻辑一致），再校验 INT16 / UINT16 / INT32 / UINT32 的范围；</li>
     *     <li>FLOAT / DOUBLE：原始值 = 实际值 ÷ scale（scale 为 1 时保留原值，否则按 DECIMAL128 精度），保留小数；
     *     转换为 float / double 后为 Infinity（上溢）或非零值变成 0（下溢）时失败；</li>
     *     <li>编码后的寄存器数必须与点位的寄存器数量一致，避免多写或少写相邻寄存器、线圈。</li>
     * </ul>
     *
     * @param propertyValue 属性值
     * @param point         点位配置
     * @return 原始值数组
     * @throws IllegalArgumentException 值为空、数组 / 结构体、类型不匹配、超出范围、寄存器数量不一致
     */
    public static int[] convertToRawValues(Object propertyValue, IotModbusPointRespDTO point) {
        // 1. 校验：不能为空，不支持数组、结构体
        if (propertyValue == null) {
            throw new IllegalArgumentException("值不能为空");
        }
        if (propertyValue instanceof Map || propertyValue instanceof Collection || propertyValue.getClass().isArray()) {
            throw new IllegalArgumentException("不支持数组或结构体");
        }
        String rawDataType = point.getRawDataType();
        IotModbusRawDataTypeEnum dataTypeEnum = IotModbusRawDataTypeEnum.getByType(rawDataType);
        if (dataTypeEnum == null) {
            throw new IllegalArgumentException("点位的数据类型为空或未知：" + rawDataType);
        }
        if (dataTypeEnum == IotModbusRawDataTypeEnum.STRING) {
            throw new IllegalArgumentException("不支持写入 STRING 类型的点位");
        }
        int registerCount = ObjectUtil.defaultIfNull(point.getRegisterCount(), 1);

        int[] registers;
        if (dataTypeEnum == IotModbusRawDataTypeEnum.BOOLEAN && isBooleanLiteral(propertyValue)) {
            // 2. 布尔字面量：线圈、BOOLEAN 寄存器直接写 1 / 0（不经过缩放因子）
            registers = new int[]{toBitValue(propertyValue) ? 1 : 0};
        } else {
            // 3.1 原始值 = 实际值 ÷ scale：浮点类型保留小数，其它四舍五入取整
            BigDecimal actualValue = toDecimal(propertyValue);
            BigDecimal scale = ObjectUtil.defaultIfNull(point.getScale(), BigDecimal.ONE);
            if (scale.signum() == 0) {
                throw new IllegalArgumentException("缩放因子不能为 0");
            }
            boolean floating = dataTypeEnum == IotModbusRawDataTypeEnum.FLOAT || dataTypeEnum == IotModbusRawDataTypeEnum.DOUBLE;
            BigDecimal rawValue;
            try {
                rawValue = floating ? divideFloating(actualValue, scale) : divideInteger(actualValue, scale);
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException(StrUtil.format("{} 超出范围（属性值 {}）", rawDataType, propertyValue));
            }
            // 3.2 校验原始值的范围，再编码为寄存器值
            validateRawValueRange(rawValue, dataTypeEnum, propertyValue);
            registers = encodeToRegisters(rawValue, rawDataType, point.getByteOrder(), registerCount);
        }
        // 4. 寄存器数必须与点位一致
        if (registers.length != registerCount) {
            throw new IllegalArgumentException(StrUtil.format("数据类型 {} 需要 {} 个寄存器（线圈），点位配置为 {} 个",
                    rawDataType, registers.length, registerCount));
        }
        return registers;
    }

    /**
     * 整数类型（含 BOOLEAN）的商超过该值时一定超出范围：最大的 UINT32 约 4.3e9
     */
    private static final BigDecimal INTEGER_QUOTIENT_LIMIT = new BigDecimal("1e12");
    /**
     * 整数类型（含 BOOLEAN）的商小于该值时四舍五入一定为 0
     */
    private static final BigDecimal INTEGER_QUOTIENT_ZERO = new BigDecimal("0.25");

    /**
     * 浮点类型的原始值：scale 为 1 时保留原值（不舍入），否则按 DECIMAL128（34 位有效数字，高于 double 的精度）相除
     */
    private static BigDecimal divideFloating(BigDecimal actualValue, BigDecimal scale) {
        return scale.compareTo(BigDecimal.ONE) == 0 ? actualValue : actualValue.divide(scale, MathContext.DECIMAL128);
    }

    /**
     * 整数类型（含 BOOLEAN）的原始值：实际值 ÷ scale 四舍五入取整（与原逻辑一致）
     * <p>
     * 先用有限精度的商判断量级：极大（例如 "1e1000000000"）时直接交给范围校验失败（BOOLEAN 为非 0），
     * 极小（例如 "1e-1000000000"）时为 0，避免按 0 位小数精确相除时分配巨大的 BigInteger
     */
    private static BigDecimal divideInteger(BigDecimal actualValue, BigDecimal scale) {
        BigDecimal quotient = actualValue.divide(scale, MathContext.DECIMAL128);
        if (quotient.abs().compareTo(INTEGER_QUOTIENT_LIMIT) > 0) {
            return quotient;
        }
        if (quotient.abs().compareTo(INTEGER_QUOTIENT_ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return actualValue.divide(scale, 0, RoundingMode.HALF_UP);
    }

    /**
     * 校验原始值的范围：超出时失败，不截断
     * <p>
     * 整数类型按 [min, max] 比较；FLOAT / DOUBLE 按实际转换结果判断：Infinity（上溢）或非零值变成 0（下溢）时失败，
     * 合法的最大值（例如 3.4028235E38 舍入为 Float.MAX_VALUE）可以写入
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    private static void validateRawValueRange(BigDecimal rawValue, IotModbusRawDataTypeEnum dataTypeEnum, Object propertyValue) {
        if (dataTypeEnum == IotModbusRawDataTypeEnum.FLOAT || dataTypeEnum == IotModbusRawDataTypeEnum.DOUBLE) {
            double converted = dataTypeEnum == IotModbusRawDataTypeEnum.FLOAT ? rawValue.floatValue() : rawValue.doubleValue();
            if (Double.isInfinite(converted)) {
                throw new IllegalArgumentException(StrUtil.format("{} 原始值 {} 超出范围（属性值 {}）",
                        dataTypeEnum.getType(), rawValue, propertyValue));
            }
            if (converted == 0 && rawValue.signum() != 0) {
                throw new IllegalArgumentException(StrUtil.format("{} 原始值 {} 绝对值过小，会变成 0（属性值 {}）",
                        dataTypeEnum.getType(), rawValue, propertyValue));
            }
            return;
        }
        BigDecimal min;
        BigDecimal max;
        switch (dataTypeEnum) {
            case INT16:
                min = BigDecimal.valueOf(Short.MIN_VALUE);
                max = BigDecimal.valueOf(Short.MAX_VALUE);
                break;
            case UINT16:
                min = BigDecimal.ZERO;
                max = BigDecimal.valueOf(0xFFFF);
                break;
            case INT32:
                min = BigDecimal.valueOf(Integer.MIN_VALUE);
                max = BigDecimal.valueOf(Integer.MAX_VALUE);
                break;
            case UINT32:
                min = BigDecimal.ZERO;
                max = BigDecimal.valueOf(0xFFFFFFFFL);
                break;
            default: // BOOLEAN：非 0 即 1（原逻辑）
                return;
        }
        if (rawValue.compareTo(min) < 0 || rawValue.compareTo(max) > 0) {
            throw new IllegalArgumentException(StrUtil.format("{} 原始值 {} 超出范围 [{}, {}]（属性值 {}）",
                    dataTypeEnum.getType(), rawValue, min.toPlainString(), max.toPlainString(), propertyValue));
        }
    }

    private static boolean isBooleanLiteral(Object value) {
        if (value instanceof Boolean) {
            return true;
        }
        String str = value instanceof String ? StrUtil.trim((String) value) : null;
        return "true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str);
    }

    /**
     * 数值：数字或数字字符串；布尔、NaN、Infinity 不是数值
     */
    private static BigDecimal toDecimal(Object value) {
        if (value instanceof Number && !(value instanceof Double && (((Double) value).isNaN() || ((Double) value).isInfinite()))
                && !(value instanceof Float && (((Float) value).isNaN() || ((Float) value).isInfinite()))) {
            return new BigDecimal(value.toString());
        }
        if (value instanceof String) {
            try {
                return new BigDecimal(StrUtil.trim((String) value));
            } catch (NumberFormatException ignored) {
                // 下面统一抛出
            }
        }
        throw new IllegalArgumentException("需要数值：" + value);
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
            throw new IllegalArgumentException("点位的数据类型为空或未知：" + rawDataType);
        }
        switch (dataTypeEnum) {
            case BOOLEAN:
                // 非 0 即 1：用 signum，避免 intValue 只取低 32 位时（例如 4294967296）变成 0
                return new int[]{rawValue.signum() != 0 ? 1 : 0};
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
                throw new IllegalArgumentException("不支持写入 " + rawDataType + " 类型的点位");
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

    // ==================== 事务响应：PDU 还原与格式校验 ====================

    /**
     * 构建异常响应 PDU：[fc | 0x80, 异常码]
     *
     * @param functionCode  原功能码
     * @param exceptionCode 异常码
     * @return 异常响应 PDU
     */
    public static byte[] buildExceptionPdu(int functionCode, int exceptionCode) {
        return new byte[]{(byte) (functionCode | FC_EXCEPTION_MASK), (byte) exceptionCode};
    }

    /**
     * 从解码后的响应帧还原响应 PDU（不带 slaveId、CRC、MBAP）
     * <p>
     * 解码器的 {@link IotModbusFrame#getPdu()} 是功能码之后的全部数据（读响应含 byteCount）；
     * 异常响应时解码器已把功能码还原为原功能码、异常码单独存放，这里重新加上 0x80 异常位
     *
     * @param frame 响应帧
     * @return 异常 → [fc | 0x80, 异常码]；正常 → [fc] + pdu
     */
    public static byte[] buildResponsePdu(IotModbusFrame frame) {
        if (frame.isException()) {
            return buildExceptionPdu(frame.getFunctionCode(), frame.getExceptionCode());
        }
        byte[] data = frame.getPdu() != null ? frame.getPdu() : new byte[0];
        byte[] responsePdu = new byte[1 + data.length];
        responsePdu[0] = (byte) frame.getFunctionCode();
        System.arraycopy(data, 0, responsePdu, 1, data.length);
        return responsePdu;
    }

    /**
     * 获取透传响应对应的设备错误：用于回复 biz 时的结果码
     *
     * @param responsePdu 设备响应 PDU（非空）
     * @return 异常响应 [fc | 0x80, 异常码] → {@link ErrorCodeConstants#MODBUS_EXCEPTION}（带异常码）；正常响应 → null
     */
    public static ErrorCode getRawResponseError(byte[] responsePdu) {
        if (!isExceptionResponse(responsePdu[0] & 0xFF) || responsePdu.length < 2) {
            return null;
        }
        ErrorCode exception = ErrorCodeConstants.MODBUS_EXCEPTION;
        return new ErrorCode(exception.getCode(), StrUtil.format(exception.getMsg(), responsePdu[1] & 0xFF));
    }

    /**
     * 获取请求 PDU 中的数量：FC01-04 / FC15 / FC16 取第 3-4 字节；FC05 / FC06 固定为 1
     *
     * @param requestPdu 请求 PDU（已通过格式校验，至少 5 字节）
     * @return 数量
     */
    public static int getRequestQuantity(byte[] requestPdu) {
        int functionCode = requestPdu[0] & 0xFF;
        if (functionCode == FC_WRITE_SINGLE_COIL || functionCode == FC_WRITE_SINGLE_REGISTER) {
            return 1;
        }
        return ((requestPdu[3] & 0xFF) << 8) | (requestPdu[4] & 0xFF);
    }

    /**
     * 校验正常响应 PDU 的格式是否与请求一致（异常响应不经过这里）
     * <ul>
     *     <li>FC01 / FC02：byteCount = ceil(数量 / 8)，总长 = 2 + byteCount；</li>
     *     <li>FC03 / FC04：byteCount = 2 × 数量，总长 = 2 + byteCount；</li>
     *     <li>FC05 / FC06：原样回显请求；</li>
     *     <li>FC15 / FC16：回显地址和数量。</li>
     * </ul>
     *
     * @param requestPdu  请求 PDU
     * @param responsePdu 响应 PDU
     * @return 是否合法
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    public static boolean isResponseShapeValid(byte[] requestPdu, byte[] responsePdu) {
        if (requestPdu == null || requestPdu.length < 5 || responsePdu == null || responsePdu.length < 1) {
            return false;
        }
        int functionCode = requestPdu[0] & 0xFF;
        if ((responsePdu[0] & 0xFF) != functionCode) {
            return false;
        }
        int quantity = getRequestQuantity(requestPdu);
        switch (functionCode) {
            case FC_READ_COILS:
            case FC_READ_DISCRETE_INPUTS:
                return isReadResponseShapeValid(responsePdu, (quantity + 7) / 8);
            case FC_READ_HOLDING_REGISTERS:
            case FC_READ_INPUT_REGISTERS:
                return isReadResponseShapeValid(responsePdu, quantity * 2);
            case FC_WRITE_SINGLE_COIL:
            case FC_WRITE_SINGLE_REGISTER:
                return responsePdu.length == 5 && Arrays.equals(Arrays.copyOf(requestPdu, 5), responsePdu);
            case FC_WRITE_MULTIPLE_COILS:
            case FC_WRITE_MULTIPLE_REGISTERS:
                return responsePdu.length == 5
                        && Arrays.equals(Arrays.copyOfRange(requestPdu, 1, 5), Arrays.copyOfRange(responsePdu, 1, 5));
            default:
                return false;
        }
    }

    private static boolean isReadResponseShapeValid(byte[] responsePdu, int expectedByteCount) {
        return responsePdu.length == 2 + expectedByteCount && (responsePdu[1] & 0xFF) == expectedByteCount;
    }

    // ==================== 寄存器位 ====================

    /**
     * 是否是寄存器位点位：FC03/FC04、BOOLEAN、1 个寄存器，且 bitIndex 在 0-15
     * <p>
     * 与管理后台、前端的位点位规则一致；不满足时（例如历史脏数据）按原逻辑处理：整个寄存器非 0 即 true、整字写。
     * 需求来源：https://t.zsxq.com/Qg5LP
     */
    public static boolean isBitPoint(IotModbusPointRespDTO point) {
        Integer bitIndex = point.getBitIndex();
        return bitIndex != null && bitIndex >= 0 && bitIndex <= 15
                && ObjectUtils.equalsAny(point.getFunctionCode(), FC_READ_HOLDING_REGISTERS, FC_READ_INPUT_REGISTERS)
                && IotModbusRawDataTypeEnum.BOOLEAN.getType().equals(point.getRawDataType())
                && ObjectUtil.equal(point.getRegisterCount(), 1);
    }

    public static boolean getBit(int registerValue, int bitIndex) {
        return ((registerValue >> bitIndex) & 1) == 1;
    }

    /**
     * 只修改寄存器的 bitIndex 位，其它位保持不变
     */
    public static int setBit(int registerValue, int bitIndex, boolean value) {
        int mask = 1 << bitIndex;
        return (value ? registerValue | mask : registerValue & ~mask) & 0xFFFF;
    }

    /**
     * 把位点位的下发值解析为布尔：支持 Boolean、"true"/"false"、数字（0 为 false，非 0 为 true）；只用于位点位，不影响原有写入转换
     */
    public static boolean toBitValue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String str = StrUtil.trim(String.valueOf(value));
        if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
            return Boolean.parseBoolean(str);
        }
        return new BigDecimal(str).signum() != 0;
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

    /**
     * 两个点位的写入目标是否一致：点位编号、功能码、地址、寄存器数量、位索引、字节序、数据类型、缩放因子都相同
     * （current 由调用方按标识符查找，标识符不再比较）
     * <p>
     * 用于发送排队中的写入前核对点位仍启用、配置未变化（名称、轮询间隔等与写入无关的字段不比较）
     *
     * @param expected 下发时使用的点位
     * @param current  当前配置中的点位（可为空：点位已禁用或删除）
     * @return 是否一致
     */
    public static boolean isSameWriteTarget(IotModbusPointRespDTO expected, IotModbusPointRespDTO current) {
        if (expected == null || current == null) {
            return false;
        }
        return ObjectUtil.equal(expected.getId(), current.getId())
                && ObjectUtil.equal(expected.getFunctionCode(), current.getFunctionCode())
                && ObjectUtil.equal(expected.getRegisterAddress(), current.getRegisterAddress())
                && ObjectUtil.equal(expected.getRegisterCount(), current.getRegisterCount())
                && ObjectUtil.equal(expected.getBitIndex(), current.getBitIndex())
                && ObjectUtil.equal(expected.getByteOrder(), current.getByteOrder())
                && ObjectUtil.equal(expected.getRawDataType(), current.getRawDataType())
                && (expected.getScale() == null ? current.getScale() == null
                : current.getScale() != null && expected.getScale().compareTo(current.getScale()) == 0);
    }

}
