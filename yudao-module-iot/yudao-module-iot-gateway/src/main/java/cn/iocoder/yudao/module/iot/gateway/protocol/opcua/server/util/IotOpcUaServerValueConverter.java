package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server.util;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.eclipse.milo.opcua.stack.core.NodeIds;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

/**
 * IoT OPC UA Server 值转换器：物模型属性的值 → OPC UA 节点的值
 * <ul>
 *     <li>int、enum → Int32；float → Float；double → Double；bool → Boolean；text → String；date → DateTime；</li>
 *     <li>struct、array 和未知类型 → String（JSON）。</li>
 * </ul>
 * 值与数据类型不匹配（例如 int 超出范围、非数字字符串，float、double 溢出为 Infinity 或非零值下溢为 0）时返回 null，
 * 由调用方把节点状态置为 Bad
 *
 * @author 芋道源码
 */
public class IotOpcUaServerValueConverter {

    private static final BigDecimal INT_MIN = BigDecimal.valueOf(Integer.MIN_VALUE);
    private static final BigDecimal INT_MAX = BigDecimal.valueOf(Integer.MAX_VALUE);
    /**
     * int 整数部分的最大位数（Integer.MAX_VALUE = 2147483647 共 10 位）
     */
    private static final int MAX_INT_DIGITS = 10;
    private static final BigDecimal LONG_MIN = BigDecimal.valueOf(Long.MIN_VALUE);
    private static final BigDecimal LONG_MAX = BigDecimal.valueOf(Long.MAX_VALUE);

    private IotOpcUaServerValueConverter() {
    }

    /**
     * 物模型数据类型 → OPC UA DataType
     *
     * @param dataType 物模型数据类型（IotDataSpecsDataTypeEnum）
     * @return OPC UA DataType 的 NodeId
     */
    public static NodeId toDataTypeId(String dataType) {
        switch (StrUtil.nullToEmpty(dataType)) {
            case "int":
            case "enum":
                return NodeIds.Int32;
            case "float":
                return NodeIds.Float;
            case "double":
                return NodeIds.Double;
            case "bool":
                return NodeIds.Boolean;
            case "date":
                return NodeIds.DateTime;
            default:
                return NodeIds.String;
        }
    }

    /**
     * 物模型属性的值 → OPC UA Variant
     *
     * @param value    属性值（JSON 反序列化得到的 Boolean、数字、字符串、Map、List）
     * @param dataType 物模型数据类型
     * @return Variant；值为空或与数据类型不匹配时返回 null
     */
    public static Variant toVariant(Object value, String dataType) {
        if (value == null) {
            return null;
        }
        try {
            switch (StrUtil.nullToEmpty(dataType)) {
                case "int":
                case "enum": {
                    Integer result = toInt(toDecimal(value));
                    return result != null ? new Variant(result) : null;
                }
                case "float": {
                    BigDecimal decimal = toDecimal(value);
                    float result = decimal.floatValue();
                    return isRepresentable(result, decimal) ? new Variant(result) : null;
                }
                case "double": {
                    BigDecimal decimal = toDecimal(value);
                    double result = decimal.doubleValue();
                    return isRepresentable(result, decimal) ? new Variant(result) : null;
                }
                case "bool":
                    Boolean bool = toBoolean(value);
                    return bool != null ? new Variant(bool) : null;
                case "date":
                    return new Variant(toDateTime(value));
                default:
                    return new Variant(value instanceof Map || value instanceof Collection
                            ? JsonUtils.toJsonString(value) : String.valueOf(value));
            }
        } catch (ArithmeticException | IllegalArgumentException | DateTimeException e) {
            return null;
        }
    }

    /**
     * 浮点转换的结果是否有效：溢出为 Infinity、非零值下溢为 0 时无效
     *
     * @param result 转换结果（float 自动拓宽为 double）
     * @param source 原始值
     * @return 是否有效
     */
    private static boolean isRepresentable(double result, BigDecimal source) {
        return !Double.isInfinite(result) && !Double.isNaN(result) && (result != 0 || source.signum() == 0);
    }

    /**
     * 数值 → int：先按整数部分的位数和 int 范围校验，再精确转换
     * <p>
     * 1E+1000000000、1E-1000000000 这类极端指数只按 precision、scale 判定，不展开成巨大整数或做大规模舍入；
     * 通过校验的值整数部分不超过 10 位、scale 不超过 precision，转换开销只与输入的有效数字位数有关
     *
     * @param decimal 数值
     * @return int 值；超出 int 范围或带非零小数时返回 null
     */
    private static Integer toInt(BigDecimal decimal) {
        if (decimal.signum() == 0) {
            return 0;
        }
        // 整数部分位数：超过 10 位必然超出 int 范围；不超过 0 位表示 0 < |x| < 1，不是整数
        long integerDigits = (long) decimal.precision() - decimal.scale();
        if (integerDigits > MAX_INT_DIGITS || integerDigits <= 0) {
            return null;
        }
        if (decimal.compareTo(INT_MIN) < 0 || decimal.compareTo(INT_MAX) > 0) {
            return null;
        }
        // 带非零小数（如 1.5）时抛出 ArithmeticException，由 toVariant 按不匹配处理；12.0 之类正常转换
        return decimal.intValueExact();
    }

    private static BigDecimal toDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        if (value instanceof Number) {
            // Double、Float 的 NaN、Infinity 在这里抛出 NumberFormatException
            return new BigDecimal(value.toString());
        }
        if (value instanceof String) {
            return new BigDecimal(StrUtil.trim((String) value));
        }
        throw new IllegalArgumentException("需要数值：" + value);
    }

    private static Boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String text = StrUtil.trim(String.valueOf(value));
        if ("true".equalsIgnoreCase(text) || "1".equals(text)) {
            return true;
        }
        if ("false".equalsIgnoreCase(text) || "0".equals(text)) {
            return false;
        }
        return null;
    }

    private static DateTime toDateTime(Object value) {
        if (value instanceof Number) {
            // 超出 long 范围的毫秒时间戳不截断，按不匹配处理
            BigDecimal millis = toDecimal(value);
            if (millis.compareTo(LONG_MIN) < 0 || millis.compareTo(LONG_MAX) > 0) {
                throw new ArithmeticException("时间戳超出范围：" + value);
            }
            // 0 < |x| < 1 的小数直接截断为 0，避免 1E-1000000000 之类在 longValue 中做大规模舍入；
            // 其余值已在 long 范围内，整数部分不超过 19 位、scale 不超过 precision
            long integerDigits = (long) millis.precision() - millis.scale();
            return new DateTime(Instant.ofEpochMilli(integerDigits <= 0 ? 0L : millis.longValue()));
        }
        String text = StrUtil.trim(String.valueOf(value));
        if (StrUtil.isNumeric(text)) {
            return new DateTime(Instant.ofEpochMilli(Long.parseLong(text)));
        }
        return new DateTime(Instant.parse(text));
    }

}
