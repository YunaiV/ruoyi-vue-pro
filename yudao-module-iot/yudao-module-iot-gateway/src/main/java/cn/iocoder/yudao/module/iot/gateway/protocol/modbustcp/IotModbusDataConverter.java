package cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotModbusByteOrderEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotModbusRawDataTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * IoT Modbus 数据转换器，负责：
 * 1. 将 Modbus 原始寄存器值转换为物模型属性值
 * 2. 将物模型属性值转换为 Modbus 原始寄存器值
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusDataConverter {

    /**
     * 将原始值转换为物模型属性值
     *
     * @param rawValues 原始值数组（寄存器值或线圈值）
     * @param point     点位配置
     * @return 转换后的属性值
     */
    public Object convertToPropertyValue(int[] rawValues, IotModbusPointRespDTO point) {
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
    public int[] convertToRawValues(Object propertyValue, IotModbusPointRespDTO point) {
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

    /**
     * 解析原始值
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    private Number parseRawValue(int[] rawValues, String rawDataType, String byteOrder) {
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

    private int parseInt32(int[] rawValues, String byteOrder) {
        if (rawValues.length < 2) {
            return rawValues[0];
        }
        byte[] bytes = reorderBytes(registersToBytes(rawValues, 2), byteOrder);
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    private long parseUint32(int[] rawValues, String byteOrder) {
        if (rawValues.length < 2) {
            return rawValues[0] & 0xFFFFFFFFL;
        }
        byte[] bytes = reorderBytes(registersToBytes(rawValues, 2), byteOrder);
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt() & 0xFFFFFFFFL;
    }

    private float parseFloat(int[] rawValues, String byteOrder) {
        if (rawValues.length < 2) {
            return (float) rawValues[0];
        }
        byte[] bytes = reorderBytes(registersToBytes(rawValues, 2), byteOrder);
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
    }

    private double parseDouble(int[] rawValues, String byteOrder) {
        if (rawValues.length < 4) {
            return rawValues[0];
        }
        byte[] bytes = reorderBytes(registersToBytes(rawValues, 4), byteOrder);
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getDouble();
    }

    /**
     * 将寄存器值转换为字节数组
     */
    private byte[] registersToBytes(int[] registers, int count) {
        byte[] bytes = new byte[count * 2];
        for (int i = 0; i < Math.min(registers.length, count); i++) {
            bytes[i * 2] = (byte) ((registers[i] >> 8) & 0xFF);
            bytes[i * 2 + 1] = (byte) (registers[i] & 0xFF);
        }
        return bytes;
    }

    /**
     * 根据字节序重排字节
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    private byte[] reorderBytes(byte[] bytes, String byteOrder) {
        IotModbusByteOrderEnum byteOrderEnum = IotModbusByteOrderEnum.getByOrder(byteOrder);
        // null 或者大端序，不需要调整
        if (ObjectUtils.equalsAny(byteOrderEnum, null, IotModbusByteOrderEnum.ABCD, IotModbusByteOrderEnum.AB)) {
            return bytes;
        }

        // 其他字节序调整
        byte[] result = new byte[bytes.length];
        switch (byteOrderEnum) {
            case BA: // 小端序（16 位）
                if (bytes.length >= 2) {
                    result[0] = bytes[1];
                    result[1] = bytes[0];
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

    /**
     * 编码为寄存器值
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    private int[] encodeToRegisters(BigDecimal rawValue, String rawDataType, String byteOrder, int registerCount) {
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
            case UINT32:
                return encodeInt32(rawValue.intValue(), byteOrder);
            case FLOAT:
                return encodeFloat(rawValue.floatValue(), byteOrder);
            case DOUBLE:
                return encodeDouble(rawValue.doubleValue(), byteOrder);
            default:
                return new int[]{rawValue.intValue()};
        }
    }

    private int[] encodeInt32(int value, String byteOrder) {
        byte[] bytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(value).array();
        bytes = reorderBytes(bytes, byteOrder);
        return bytesToRegisters(bytes);
    }

    private int[] encodeFloat(float value, String byteOrder) {
        byte[] bytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putFloat(value).array();
        bytes = reorderBytes(bytes, byteOrder);
        return bytesToRegisters(bytes);
    }

    private int[] encodeDouble(double value, String byteOrder) {
        byte[] bytes = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putDouble(value).array();
        bytes = reorderBytes(bytes, byteOrder);
        return bytesToRegisters(bytes);
    }

    private int[] bytesToRegisters(byte[] bytes) {
        int[] registers = new int[bytes.length / 2];
        for (int i = 0; i < registers.length; i++) {
            registers[i] = ((bytes[i * 2] & 0xFF) << 8) | (bytes[i * 2 + 1] & 0xFF);
        }
        return registers;
    }

    /**
     * 格式化返回值
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    private Object formatValue(BigDecimal value, String rawDataType) {
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

}
