package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.util;

import cn.hutool.core.util.StrUtil;
import org.eclipse.milo.opcua.stack.core.NodeIds;
import org.eclipse.milo.opcua.stack.core.OpcUaDataType;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.ULong;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.*;

/**
 * IoT OPC UA 值转换器：把 OPC UA 的值转换为 JSON 安全的类型，由 biz 按物模型二次转换
 * <ul>
 *     <li>Boolean、String 原样；有符号整数 → int / long；UByte、UShort、UInteger → int / long；UInt64 → 十进制字符串；</li>
 *     <li>Float、Double 原样，NaN、±Inf 丢弃；DateTime → 毫秒；ByteString → Base64；</li>
 *     <li>LocalizedText → 文本；QualifiedName → 名称；Guid → 字符串；NodeId、ExpandedNodeId → 字符串格式；StatusCode → 数值；</li>
 *     <li>一维数组 → List（元素按以上规则转换，有元素不能转换时整组丢弃）；</li>
 *     <li>ExtensionObject、多维数组（Matrix）和其它类型 → 丢弃，由调用方告警。</li>
 * </ul>
 * 写入（{@link #toVariant}）按节点实际的 DataType 把物模型的值转换为 Variant：只支持标量的内置类型，
 * 类型不匹配、超出范围、需要截断时直接失败，不做隐式截断；数组、结构体不支持
 * <p>
 * 放在 gateway 而不是 core 的 IotOpcUaUtils：读、写两个方向的转换都依赖 Milo 的内置类型（Variant、DateTime、
 * 无符号整数、OpcUaDataType 等），只有 gateway 引入 Milo；core 被 biz 依赖，不引入 OPC UA SDK
 *
 * @author 芋道源码
 */
public class IotOpcUaValueConverter {

    /**
     * 转换 OPC UA 的值
     *
     * @param value Variant 中的值
     * @return 转换后的值；不能转换（或值为空）时返回 null，调用方丢弃该点位
     */
    public static Object convert(Object value) {
        if (value == null) {
            return null;
        }
        // 1. 标量
        if (value instanceof Boolean || value instanceof String
                || value instanceof Integer || value instanceof Long) {
            return value;
        }
        if (value instanceof Byte || value instanceof Short) {
            return ((Number) value).intValue();
        }
        if (value instanceof Float) {
            float f = (Float) value;
            return Float.isNaN(f) || Float.isInfinite(f) ? null : value;
        }
        if (value instanceof Double) {
            double d = (Double) value;
            return Double.isNaN(d) || Double.isInfinite(d) ? null : value;
        }
        if (value instanceof UByte || value instanceof UShort) {
            return ((Number) value).intValue();
        }
        if (value instanceof UInteger) {
            return ((UInteger) value).longValue();
        }
        if (value instanceof ULong) {
            return ((ULong) value).toBigInteger().toString();
        }
        if (value instanceof DateTime) {
            return ((DateTime) value).getJavaTime();
        }
        if (value instanceof ByteString) {
            return Base64.getEncoder().encodeToString(((ByteString) value).bytesOrEmpty());
        }
        if (value instanceof LocalizedText) {
            return ((LocalizedText) value).getText();
        }
        if (value instanceof QualifiedName) {
            return ((QualifiedName) value).getName();
        }
        if (value instanceof UUID) {
            return value.toString();
        }
        if (value instanceof NodeId) {
            return ((NodeId) value).toParseableString();
        }
        if (value instanceof ExpandedNodeId) {
            return ((ExpandedNodeId) value).toParseableString();
        }
        if (value instanceof StatusCode) {
            return ((StatusCode) value).getValue();
        }
        if (value instanceof XmlElement) {
            return ((XmlElement) value).getFragment();
        }
        // 2. 一维数组 → List；多维数组（元素仍是数组）丢弃
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> list = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(value, i);
                if (element != null && element.getClass().isArray()) {
                    return null;
                }
                Object converted = convert(element);
                if (converted == null) {
                    return null;
                }
                list.add(converted);
            }
            return list;
        }
        // 3. ExtensionObject、Matrix 等：一期不支持
        return null;
    }

    // ========== 写入：物模型的值 → Variant ==========

    /**
     * 支持写入的内置标量类型
     */
    private static final Set<OpcUaDataType> WRITABLE_DATA_TYPES = Collections.unmodifiableSet(EnumSet.of(
            OpcUaDataType.Boolean, OpcUaDataType.SByte, OpcUaDataType.Byte, OpcUaDataType.Int16, OpcUaDataType.UInt16,
            OpcUaDataType.Int32, OpcUaDataType.UInt32, OpcUaDataType.Int64, OpcUaDataType.UInt64,
            OpcUaDataType.Float, OpcUaDataType.Double, OpcUaDataType.String, OpcUaDataType.DateTime));

    private static final BigInteger UINT32_MAX = BigInteger.valueOf(0xFFFFFFFFL);

    /**
     * 解析节点的 DataType：内置标量类型原样返回；常见子类型 UtcTime → DateTime、Duration → Double；
     * 抽象类型（BaseDataType、Number 等）、枚举、结构体和其它自定义类型返回 null（不支持写入）
     *
     * @param dataTypeId 节点 DataType 属性的值
     * @return 写入使用的内置类型；不支持时返回 null
     */
    public static OpcUaDataType resolveWritableDataType(NodeId dataTypeId) {
        if (dataTypeId == null) {
            return null;
        }
        if (NodeIds.UtcTime.equals(dataTypeId)) {
            return OpcUaDataType.DateTime;
        }
        if (NodeIds.Duration.equals(dataTypeId)) {
            return OpcUaDataType.Double;
        }
        OpcUaDataType dataType = OpcUaDataType.fromNodeId(dataTypeId);
        return dataType != null && WRITABLE_DATA_TYPES.contains(dataType) ? dataType : null;
    }

    /**
     * 把物模型的值转换为指定内置类型的 Variant
     *
     * @param value    物模型的值（JSON 反序列化得到的 Boolean、数字或字符串）
     * @param dataType 节点的内置类型（{@link #resolveWritableDataType} 的结果）
     * @return Variant
     * @throws IllegalArgumentException 值为空、数组 / 结构体、类型不匹配、超出范围或需要截断
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    public static Variant toVariant(Object value, OpcUaDataType dataType) {
        if (value == null) {
            throw new IllegalArgumentException("值不能为空");
        }
        if (value instanceof Map || value instanceof Collection || value.getClass().isArray()) {
            throw new IllegalArgumentException("不支持数组或结构体");
        }
        switch (dataType) {
            case Boolean:
                return new Variant(toBoolean(value));
            case SByte:
                return new Variant(toInteger(value, BigInteger.valueOf(Byte.MIN_VALUE), BigInteger.valueOf(Byte.MAX_VALUE)).byteValue());
            case Byte:
                return new Variant(ubyte(toInteger(value, BigInteger.ZERO, BigInteger.valueOf(255)).intValue()));
            case Int16:
                return new Variant(toInteger(value, BigInteger.valueOf(Short.MIN_VALUE), BigInteger.valueOf(Short.MAX_VALUE)).shortValue());
            case UInt16:
                return new Variant(ushort(toInteger(value, BigInteger.ZERO, BigInteger.valueOf(65535)).intValue()));
            case Int32:
                return new Variant(toInteger(value, BigInteger.valueOf(Integer.MIN_VALUE), BigInteger.valueOf(Integer.MAX_VALUE)).intValue());
            case UInt32:
                return new Variant(uint(toInteger(value, BigInteger.ZERO, UINT32_MAX).longValue()));
            case Int64:
                return new Variant(toInteger(value, BigInteger.valueOf(Long.MIN_VALUE), BigInteger.valueOf(Long.MAX_VALUE)).longValue());
            case UInt64:
                return new Variant(ulong(toInteger(value, BigInteger.ZERO, ULong.MAX_VALUE)));
            case Float:
                return new Variant(toFloat(value));
            case Double:
                return new Variant(toDouble(value));
            case String:
                if (!(value instanceof String)) {
                    throw new IllegalArgumentException("需要字符串：" + value);
                }
                return new Variant(value);
            case DateTime:
                return new Variant(toDateTime(value));
            default:
                throw new IllegalArgumentException("不支持的数据类型：" + dataType);
        }
    }

    /**
     * 布尔：true / false（含字符串，忽略大小写），或数值 0 / 1；其它值不猜测
     */
    private static boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            String str = StrUtil.trim((String) value);
            if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
                return Boolean.parseBoolean(str);
            }
        }
        BigDecimal number = toDecimalOrNull(value);
        if (number != null && (number.compareTo(BigDecimal.ZERO) == 0 || number.compareTo(BigDecimal.ONE) == 0)) {
            return number.compareTo(BigDecimal.ONE) == 0;
        }
        throw new IllegalArgumentException("需要布尔值（true / false / 0 / 1）：" + value);
    }

    /**
     * 整数的最大位数：最大的 UInt64（18446744073709551615）是 20 位，超过的一定超出范围
     */
    private static final int MAX_INTEGER_DIGITS = 20;

    /**
     * 整数：必须是整数值（12.0 可以，12.5 不可以），并且在 [min, max] 范围内
     * <p>
     * 先按有效位数、小数位数判断（例如 "1e1000000000"、"1e-1000000000"），再 toBigIntegerExact，避免分配巨大的 BigInteger
     */
    private static BigInteger toInteger(Object value, BigInteger min, BigInteger max) {
        BigDecimal number = toDecimal(value);
        if (number.signum() == 0) {
            return checkIntegerRange(BigInteger.ZERO, min, max, value);
        }
        // 1. 去掉末尾的 0 后仍有小数位：不是整数（12.50 → 12.5）
        BigDecimal stripped = number.stripTrailingZeros();
        if (stripped.scale() > 0) {
            throw new IllegalArgumentException("需要整数：" + value);
        }
        // 2. 整数位数（precision - scale，scale <= 0）超过 20 位：一定超出范围
        if ((long) stripped.precision() - stripped.scale() > MAX_INTEGER_DIGITS) {
            throw new IllegalArgumentException(StrUtil.format("超出范围 [{}, {}]：{}", min, max, value));
        }
        // 3. 位数有限后再转换、比较范围
        return checkIntegerRange(stripped.toBigIntegerExact(), min, max, value);
    }

    private static BigInteger checkIntegerRange(BigInteger integer, BigInteger min, BigInteger max, Object value) {
        if (integer.compareTo(min) < 0 || integer.compareTo(max) > 0) {
            throw new IllegalArgumentException(StrUtil.format("超出范围 [{}, {}]：{}", min, max, value));
        }
        return integer;
    }

    /**
     * Float：按 IEEE 754 舍入到最近的 Float（例如 3.4028235E38 就是 Float.MAX_VALUE）；
     * 舍入后为 Infinity（上溢）或非零值变成 0（下溢）时失败，不静默写入 Infinity、0
     */
    private static float toFloat(Object value) {
        BigDecimal number = toDecimal(value);
        float result = number.floatValue();
        if (Float.isInfinite(result)) {
            throw new IllegalArgumentException("超出 Float 范围：" + value);
        }
        if (result == 0 && number.signum() != 0) {
            throw new IllegalArgumentException("绝对值过小，Float 会变成 0：" + value);
        }
        return result;
    }

    /**
     * Double：按 IEEE 754 舍入到最近的 Double；舍入后为 Infinity（例如 "1e400"）或非零值变成 0（例如 "1e-400"）时失败
     */
    private static double toDouble(Object value) {
        BigDecimal number = toDecimal(value);
        double result = number.doubleValue();
        if (Double.isInfinite(result)) {
            throw new IllegalArgumentException("超出 Double 范围：" + value);
        }
        if (result == 0 && number.signum() != 0) {
            throw new IllegalArgumentException("绝对值过小，Double 会变成 0：" + value);
        }
        return result;
    }

    /**
     * 日期时间：毫秒时间戳（数值或数字字符串），或 ISO-8601 字符串（例如 2026-11-21T08:00:00Z）
     */
    private static DateTime toDateTime(Object value) {
        Instant instant;
        if (value instanceof String && !isDecimalString((String) value)) {
            try {
                instant = Instant.parse(StrUtil.trim((String) value));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("需要毫秒时间戳或 ISO-8601 时间：" + value);
            }
        } else {
            instant = Instant.ofEpochMilli(toInteger(value, BigInteger.valueOf(Long.MIN_VALUE),
                    BigInteger.valueOf(Long.MAX_VALUE)).longValue());
        }
        if (instant.isBefore(DateTime.MIN_ISO_8601_INSTANT) || instant.isAfter(DateTime.MAX_ISO_8601_INSTANT)) {
            throw new IllegalArgumentException("超出 OPC UA DateTime 范围：" + value);
        }
        return new DateTime(instant);
    }

    /**
     * 数值：数字或数字字符串；布尔、NaN、Infinity 不是数值
     */
    private static BigDecimal toDecimal(Object value) {
        BigDecimal number = toDecimalOrNull(value);
        if (number == null) {
            throw new IllegalArgumentException("需要数值：" + value);
        }
        return number;
    }

    /**
     * 数值：数字或十进制数字字符串转为 BigDecimal；布尔、NaN、Infinity 和其它类型返回 null
     */
    private static BigDecimal toDecimalOrNull(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        if (value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte) {
            return BigDecimal.valueOf(((Number) value).longValue());
        }
        if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();
            return Double.isNaN(d) || Double.isInfinite(d) ? null : new BigDecimal(value.toString());
        }
        if (value instanceof String && isDecimalString((String) value)) {
            return new BigDecimal(StrUtil.trim((String) value));
        }
        return null;
    }

    /**
     * 是否是十进制数字字符串：只接受 BigDecimal 能解析的写法（不接受 0x、NaN、Infinity）
     */
    private static boolean isDecimalString(String str) {
        String trimmed = StrUtil.trim(str);
        if (StrUtil.isEmpty(trimmed)) {
            return false;
        }
        try {
            new BigDecimal(trimmed);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
