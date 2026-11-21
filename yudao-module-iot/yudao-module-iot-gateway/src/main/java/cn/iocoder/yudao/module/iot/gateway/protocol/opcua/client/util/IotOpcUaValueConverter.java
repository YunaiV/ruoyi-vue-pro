package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.util;

import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.ULong;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

// TODO @AI：搞到 utils 里？目前有 一个呀；
/**
 * IoT OPC UA 值转换器：把 OPC UA 的值转换为 JSON 安全的类型，由 biz 按物模型二次转换
 * <ul>
 *     <li>Boolean、String 原样；有符号整数 → int / long；UByte、UShort、UInteger → int / long；UInt64 → 十进制字符串；</li>
 *     <li>Float、Double 原样，NaN、±Inf 丢弃；DateTime → 毫秒；ByteString → Base64；</li>
 *     <li>LocalizedText → 文本；QualifiedName → 名称；Guid → 字符串；NodeId、ExpandedNodeId → 字符串格式；StatusCode → 数值；</li>
 *     <li>一维数组 → List（元素按以上规则转换，有元素不能转换时整组丢弃）；</li>
 *     <li>ExtensionObject、多维数组（Matrix）和其它类型 → 丢弃，由调用方告警。</li>
 * </ul>
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

}
